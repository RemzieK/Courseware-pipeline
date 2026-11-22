package bg.tu.unicloud;

import bg.tu.unicloud.dto.*;
import bg.tu.unicloud.model.MaterialType;
import bg.tu.unicloud.model.Role;
import bg.tu.unicloud.security.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CoursewareApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String getTestToken(String username, String role, String email, String sub) {
        return jwtTokenService.createToken(
                sub,
                email,
                username,
                role,
                List.of(101L),
                3600
        );
    }

    @Test
    @DisplayName("Public catalog access with pagination and cache headers")
    void testPublicCoursesCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/courses")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("public")))
                .andExpect(header().string("Cache-Control", containsString("max-age=60")))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("Rejects invalid pagination parameters with 400 Bad Request")
    void testPaginationValidation() throws Exception {
        // Negative page
        mockMvc.perform(get("/api/v1/courses").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

        // Size over 100 cap
        mockMvc.perform(get("/api/v1/courses").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

        // Unsupported sort field
        mockMvc.perform(get("/api/v1/courses").param("sort", "malicious_col,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401 Unauthorized")
    void testUnauthenticatedAccess() throws Exception {
        CourseCreateRequest request = CourseCreateRequest.builder()
                .code("SEC-01")
                .title("Security Test Course")
                .discipline("CS")
                .semester("2026W")
                .build();

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Token with unsupported role (DEAN/INSPECTOR) returns 403 Forbidden")
    void testUnsupportedRoleRejection() throws Exception {
        String deanToken = getTestToken("dean1", "DEAN", "dean1@tu-sofia.bg", "usr-dean-1");

        mockMvc.perform(get("/api/v1/courses")
                        .header("Authorization", "Bearer " + deanToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Course creation, hierarchy, role checks and ownership enforcement")
    void testCourseLifecycleAndOwnership() throws Exception {
        String teacher1Token = getTestToken("teacher1", "TEACHER", "teacher1@tu-sofia.bg", "usr-teacher-1");
        String teacher2Token = getTestToken("teacher2", "TEACHER", "teacher2@tu-sofia.bg", "usr-teacher-2");
        String student1Token = getTestToken("student1", "STUDENT", "student1@tu-sofia.bg", "usr-student-1");
        String adminToken = getTestToken("admin1", "ADMIN", "admin1@tu-sofia.bg", "usr-admin-1");

        // 1. Student cannot create a course -> 403
        CourseCreateRequest courseReq = CourseCreateRequest.builder()
                .code("OIT-" + UUID.randomUUID().toString().substring(0, 8))
                .title("Облачни изчисления")
                .discipline("Информатика")
                .semester("2026W")
                .externalCourseOfferingId(101L)
                .build();

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseReq)))
                .andExpect(status().isForbidden());

        // 2. Teacher 1 creates the course -> 201 Created
        String createResponse = mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + teacher1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.code").value(courseReq.getCode()))
                .andReturn().getResponse().getContentAsString();

        CourseResponse createdCourse = objectMapper.readValue(createResponse, CourseResponse.class);
        UUID courseId = createdCourse.getId();

        // 3. Teacher 2 tries to update Teacher 1's course -> 403 Forbidden
        CourseUpdateRequest updateReq = CourseUpdateRequest.builder()
                .title("Хакнат курс")
                .discipline("Информатика")
                .semester("2026W")
                .build();

        mockMvc.perform(put("/api/v1/courses/" + courseId)
                        .header("Authorization", "Bearer " + teacher2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        // 4. Admin updates Teacher 1's course -> 200 OK
        CourseUpdateRequest adminUpdateReq = CourseUpdateRequest.builder()
                .title("Облачни изчисления (Ревизиран от Админ)")
                .discipline("Информатика")
                .semester("2026W")
                .build();

        mockMvc.perform(put("/api/v1/courses/" + courseId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUpdateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Облачни изчисления (Ревизиран от Админ)"));

        // 5. Teacher 1 creates a Module -> 201 Created
        ModuleCreateRequest moduleReq = ModuleCreateRequest.builder()
                .title("Седмица 1 – Архитектура")
                .description("Основни принципи")
                .orderIndex(1)
                .build();

        String moduleRes = mockMvc.perform(post("/api/v1/courses/" + courseId + "/modules")
                        .header("Authorization", "Bearer " + teacher1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        ModuleResponse createdModule = objectMapper.readValue(moduleRes, ModuleResponse.class);
        UUID moduleId = createdModule.getId();

        // 6. Teacher 1 creates a Lesson -> 201 Created
        LessonCreateRequest lessonReq = LessonCreateRequest.builder()
                .title("Лекция 1: Въведение")
                .description("Въведение в микроуслугите")
                .orderIndex(1)
                .build();

        String lessonRes = mockMvc.perform(post("/api/v1/modules/" + moduleId + "/lessons")
                        .header("Authorization", "Bearer " + teacher1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        LessonResponse createdLesson = objectMapper.readValue(lessonRes, LessonResponse.class);
        UUID lessonId = createdLesson.getId();

        // 7. Teacher 1 creates an ASSIGNMENT material -> 201 Created
        MaterialCreateRequest assignmentReq = MaterialCreateRequest.builder()
                .type(MaterialType.ASSIGNMENT)
                .title("Курсова задача 1 – Docker")
                .description("Условие на задачата за контейнеризация")
                .tags(List.of("docker", "devops"))
                .fileUrl("https://storage.unicloud.tu-sofia.bg/files/docker-task.pdf")
                .fileName("docker-task.pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(102400L)
                .fileHash("hash123456")
                .hidden(false)
                .build();

        String materialRes = mockMvc.perform(post("/api/v1/lessons/" + lessonId + "/materials")
                        .header("Authorization", "Bearer " + teacher1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ASSIGNMENT"))
                .andExpect(jsonPath("$.title").value("Курсова задача 1 – Docker"))
                .andReturn().getResponse().getContentAsString();

        MaterialResponse createdMaterial = objectMapper.readValue(materialRes, MaterialResponse.class);
        UUID materialId = createdMaterial.getId();

        // 8. Teacher 1 adds a new version to the material -> 201 Created
        MaterialVersionCreateRequest versionReq = MaterialVersionCreateRequest.builder()
                .fileUrl("https://storage.unicloud.tu-sofia.bg/files/docker-task-v2.pdf")
                .fileName("docker-task-v2.pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(105000L)
                .fileHash("hash789012")
                .build();

        mockMvc.perform(post("/api/v1/materials/" + materialId + "/versions")
                        .header("Authorization", "Bearer " + teacher1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(versionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(2));

        // 9. Public course detail retrieval shows full hierarchy
        mockMvc.perform(get("/api/v1/courses/" + courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules").isArray())
                .andExpect(jsonPath("$.modules[0].title").value("Седмица 1 – Архитектура"))
                .andExpect(jsonPath("$.modules[0].lessons[0].title").value("Лекция 1: Въведение"));

        // 10. Student marks material as read -> 200 OK + private, no-store cache header
        mockMvc.perform(put("/api/v1/materials/" + materialId + "/read")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(jsonPath("$.materialId").value(materialId.toString()));

        // Student checks course progress
        mockMvc.perform(get("/api/v1/courses/" + courseId + "/progress")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(jsonPath("$.readMaterials").value(1))
                .andExpect(jsonPath("$.progressPercentage").value(100.0));

        // 11. Service role restrictions test
        String serviceToken = getTestToken("service-judge", "SERVICE", "judge@autojudge.tu-sofia.bg", "usr-service-judge");

        // Service can access integrations assignments
        mockMvc.perform(get("/api/v1/integrations/assignments")
                        .header("Authorization", "Bearer " + serviceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Student cannot access integrations assignments -> 403
        mockMvc.perform(get("/api/v1/integrations/assignments")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());

        // Service can access ASSIGNMENT file
        mockMvc.perform(get("/api/v1/materials/" + materialId + "/file")
                        .header("Authorization", "Bearer " + serviceToken))
                .andExpect(status().isFound());

        // Teacher creates a NOTES material
        MaterialCreateRequest notesReq = MaterialCreateRequest.builder()
                .type(MaterialType.NOTES)
                .title("Лекционни записки")
                .fileUrl("https://storage.unicloud.tu-sofia.bg/files/notes.pdf")
                .fileName("notes.pdf")
                .mimeType("application/pdf")
                .hidden(false)
                .build();

        String notesRes = mockMvc.perform(post("/api/v1/lessons/" + lessonId + "/materials")
                        .header("Authorization", "Bearer " + teacher1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notesReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID notesId = objectMapper.readValue(notesRes, MaterialResponse.class).getId();

        // Service trying to access NOTES file -> 403 Forbidden!
        mockMvc.perform(get("/api/v1/materials/" + notesId + "/file")
                        .header("Authorization", "Bearer " + serviceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Validation constraints return 400 with fields detail")
    void testValidationConstraints() throws Exception {
        String teacherToken = getTestToken("teacher1", "TEACHER", "teacher1@tu-sofia.bg", "usr-teacher-1");

        // Missing required fields
        CourseCreateRequest invalidReq = CourseCreateRequest.builder()
                .code("")
                .title("")
                .discipline("")
                .semester("")
                .build();

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Duplicate course code returns 409 Conflict")
    void testDuplicateCourseCode() throws Exception {
        String teacherToken = getTestToken("teacher1", "TEACHER", "teacher1@tu-sofia.bg", "usr-teacher-1");

        String code = "DUP-" + UUID.randomUUID().toString().substring(0, 8);
        CourseCreateRequest req = CourseCreateRequest.builder()
                .code(code)
                .title("Course 1")
                .discipline("CS")
                .semester("2026W")
                .build();

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Duplicate code submission
        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @DisplayName("Search endpoint returns 200 with cache header")
    void testSearchEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "облак")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("public")))
                .andExpect(header().string("Cache-Control", containsString("max-age=60")))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Auth token endpoint issues token for preset profile")
    void testAuthTokenEndpoint() throws Exception {
        AuthTokenRequest request = AuthTokenRequest.builder()
                .username("student1")
                .build();

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.email").value("student1@tu-sofia.bg"));
    }

    @Test
    @DisplayName("UC-29 & US-04: Multipart file upload, versioning, download, and student read update tracking")
    void testFileUploadVersioningAndSecurityLifecycle() throws Exception {
        String teacherToken = getTestToken("teacher1", "TEACHER", "teacher1@tu-sofia.bg", "usr-teacher-1");
        String studentToken = getTestToken("student1", "STUDENT", "student1@tu-sofia.bg", "usr-student-1");

        // 1. Create Course, Module, Lesson
        String code = "UC29-" + UUID.randomUUID().toString().substring(0, 8);
        CourseCreateRequest courseReq = CourseCreateRequest.builder()
                .code(code)
                .title("Cloud File Upload Testing")
                .discipline("CS")
                .semester("2026W")
                .build();

        String courseJson = mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID courseId = objectMapper.readValue(courseJson, CourseResponse.class).getId();

        ModuleCreateRequest modReq = ModuleCreateRequest.builder().title("Module Upload").orderIndex(1).build();
        String modJson = mockMvc.perform(post("/api/v1/courses/" + courseId + "/modules")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID moduleId = objectMapper.readValue(modJson, ModuleResponse.class).getId();

        LessonCreateRequest lessonReq = LessonCreateRequest.builder().title("Lesson 1 Files").orderIndex(1).build();
        String lessonJson = mockMvc.perform(post("/api/v1/modules/" + moduleId + "/lessons")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID lessonId = objectMapper.readValue(lessonJson, LessonResponse.class).getId();

        // 2. Upload initial material via multipart/form-data
        byte[] v1Bytes = "%PDF-1.7 Lecture 1 test content bytes".getBytes();
        MockMultipartFile v1File = new MockMultipartFile("file", "lecture1.pdf", "application/pdf", v1Bytes);

        String matJson = mockMvc.perform(multipart("/api/v1/lessons/" + lessonId + "/materials")
                        .file(v1File)
                        .param("title", "Лекция 1 – Въведение в облака")
                        .param("type", "PRESENTATION")
                        .param("description", "Слайдове за първа лекция")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentVersion").value(1))
                .andExpect(jsonPath("$.fileUrl").value(containsString("/api/v1/materials/")))
                .andReturn().getResponse().getContentAsString();

        UUID materialId = objectMapper.readValue(matJson, MaterialResponse.class).getId();

        // 3. Student marks material v1 as read
        mockMvc.perform(put("/api/v1/materials/" + materialId + "/read")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionAtRead").value(1))
                .andExpect(jsonPath("$.hasNewVersion").value(false));

        // Course progress shows 100%
        mockMvc.perform(get("/api/v1/courses/" + courseId + "/progress")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readMaterials").value(1))
                .andExpect(jsonPath("$.progressPercentage").value(100.0));

        // 4. Teacher uploads version 2 via multipart/form-data
        byte[] v2Bytes = "%PDF-1.7 Lecture 1 updated version 2 content bytes".getBytes();
        MockMultipartFile v2File = new MockMultipartFile("file", "lecture1_v2.pdf", "application/pdf", v2Bytes);

        mockMvc.perform(multipart("/api/v1/materials/" + materialId + "/versions")
                        .file(v2File)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.fileName").value("lecture1_v2.pdf"));

        // 5. Version history shows both versions
        mockMvc.perform(get("/api/v1/materials/" + materialId + "/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].versionNumber").value(2))
                .andExpect(jsonPath("$[1].versionNumber").value(1));

        // 6. US-04 check: student progress now reflects update available (hasNewVersion: true)
        mockMvc.perform(get("/api/v1/courses/" + courseId + "/progress")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readMarks[0].hasNewVersion").value(true))
                .andExpect(jsonPath("$.readMaterials").value(0))
                .andExpect(jsonPath("$.progressPercentage").value(0.0));

        // 7. Download file checks (v1, v2, and current default)
        mockMvc.perform(get("/api/v1/materials/" + materialId + "/file").param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("lecture1.pdf")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("max-age=86400")))
                .andExpect(content().bytes(v1Bytes));

        mockMvc.perform(get("/api/v1/materials/" + materialId + "/file").param("version", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("lecture1_v2.pdf")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("max-age=86400")))
                .andExpect(content().bytes(v2Bytes));

        mockMvc.perform(get("/api/v1/materials/" + materialId + "/file"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(content().bytes(v2Bytes));
    }

    @Test
    @DisplayName("UC-13: Safe upload controls reject executables (415) and oversized files (413)")
    void testFileSecurityPolicyRejections() throws Exception {
        String teacherToken = getTestToken("teacher1", "TEACHER", "teacher1@tu-sofia.bg", "usr-teacher-1");

        // Create prerequisite course, module, lesson
        String code = "SEC-" + UUID.randomUUID().toString().substring(0, 8);
        CourseCreateRequest courseReq = CourseCreateRequest.builder()
                .code(code)
                .title("Security Upload Policy Course")
                .discipline("CS")
                .semester("2026W")
                .build();

        String courseJson = mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID courseId = objectMapper.readValue(courseJson, CourseResponse.class).getId();

        ModuleCreateRequest modReq = ModuleCreateRequest.builder().title("Security Module").orderIndex(1).build();
        String modJson = mockMvc.perform(post("/api/v1/courses/" + courseId + "/modules")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID moduleId = objectMapper.readValue(modJson, ModuleResponse.class).getId();

        LessonCreateRequest lessonReq = LessonCreateRequest.builder().title("Security Lesson").orderIndex(1).build();
        String lessonJson = mockMvc.perform(post("/api/v1/modules/" + moduleId + "/lessons")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID lessonId = objectMapper.readValue(lessonJson, LessonResponse.class).getId();

        // 1. Explicit executable file (.exe) -> 415 Unsupported Media Type
        MockMultipartFile exeFile = new MockMultipartFile("file", "trojan.exe", "application/octet-stream",
                new byte[]{0x4D, 0x5A, 0x00, 0x00});

        mockMvc.perform(multipart("/api/v1/lessons/" + lessonId + "/materials")
                        .file(exeFile)
                        .param("title", "Trojan Attempt")
                        .param("type", "PRESENTATION")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        // 2. Disguised executable (.exe renamed to .pdf with MZ header) -> 415 Unsupported Media Type
        MockMultipartFile disguisedExe = new MockMultipartFile("file", "renamed_virus.pdf", "application/pdf",
                new byte[]{0x4D, 0x5A, 0x11, 0x22});

        mockMvc.perform(multipart("/api/v1/lessons/" + lessonId + "/materials")
                        .file(disguisedExe)
                        .param("title", "Disguised Virus Attempt")
                        .param("type", "PRESENTATION")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        // 3. Disguised PHP script renamed to .docx -> 415 Unsupported Media Type
        MockMultipartFile disguisedPhp = new MockMultipartFile("file", "payload.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "<?php phpinfo(); ?>".getBytes());

        mockMvc.perform(multipart("/api/v1/lessons/" + lessonId + "/materials")
                        .file(disguisedPhp)
                        .param("title", "Disguised PHP Attempt")
                        .param("type", "NOTES")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        // 4. File over 50 MB -> 413 Payload Too Large
        MockMultipartFile oversizedFile = new MockMultipartFile("file", "huge_presentation.pdf", "application/pdf", new byte[16]) {
            @Override
            public long getSize() {
                return 51 * 1024 * 1024L; // 51 MB
            }
        };

        mockMvc.perform(multipart("/api/v1/lessons/" + lessonId + "/materials")
                        .file(oversizedFile)
                        .param("title", "Huge File Attempt")
                        .param("type", "PRESENTATION")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.error").value("PAYLOAD_TOO_LARGE"));
    }
}

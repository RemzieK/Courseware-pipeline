import {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";

const AuthContext = createContext(null);
const STORAGE_KEY = "unicloud_auth";

function readStoredSession() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (!stored) return null;

    const session = JSON.parse(stored);

    if (
      !session.token ||
      !session.expiresAt ||
      new Date(session.expiresAt).getTime() <= Date.now()
    ) {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }

    return session;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => readStoredSession());

  function login(tokenData, username) {
    const session = {
      token: tokenData.token,
      tokenType: tokenData.tokenType || "Bearer",
      userId: tokenData.userId,
      email: tokenData.email,
      role: tokenData.role,
      expiresAt: tokenData.expiresAt,
      username,
    };

    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    setUser(session);
  }

  function logout() {
    localStorage.removeItem(STORAGE_KEY);
    setUser(null);
  }

  useEffect(() => {
    function handleUnauthorized() {
      logout();
    }

    window.addEventListener("auth:unauthorized", handleUnauthorized);

    return () => {
      window.removeEventListener("auth:unauthorized", handleUnauthorized);
    };
  }, []);

  const value = {
    user,
    isAuthenticated: Boolean(user?.token),
    login,
    logout,
    setUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth трябва да се използва в AuthProvider");
  }

  return context;
}
import { useState } from "react";
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { saveTokens } from "../utils/auth";
import { apiFetch } from "../utils/auth";
function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const [rememberMe, setRememberMe] = useState(false);
  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const res = await apiFetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, rememberMe }),
      });

      const text = await res.text();

      if (!res.ok) {
        try {
          const data = JSON.parse(text);
          throw new Error(data.message || 'Login failed');
        } catch {
          throw new Error(text || 'Login failed');
        }
      }

      const { accessToken, refreshToken } = JSON.parse(text);
        saveTokens({ accessToken, refreshToken });


      console.log('✅ Login success, navigating to /dashboard');
      navigate('/dashboard');
    } catch (err) {
      console.error('❌ Login error:', err.message);
      setError(err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-peach dark:bg-gray-900 text-gray-800 dark:text-white">
      <div className="relative bg-white dark:bg-gray-800 p-8 rounded-lg shadow-md w-full max-w-md">

        {/* Top-left link */}
        <button
          type="button"
          onClick={() => navigate('/')}
          className="absolute top-2 left-3 text-blue-600 dark:text-blue-400 text-sm hover:underline"
        >
          ← Back to Homepage
        </button>

        <h2 className="text-2xl font-semibold mb-6 text-center">Login</h2>

        {error && (
          <div className="text-sm text-red-600 dark:text-red-400 mb-4 text-center">{error}</div>
        )}

        <form onSubmit={handleLogin}>
          <div className="mb-4">
            <label className="block mb-1 font-medium text-left" htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              className="w-full border px-4 py-2 rounded bg-white dark:bg-gray-700 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-peach"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="mb-6">
            <label className="block mb-1 font-medium text-left" htmlFor="password">Password</label>
            <div className="relative">
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="w-full border px-4 py-2 rounded bg-white dark:bg-gray-700 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-peach"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-white"
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </button>
            </div>
          </div>

          <label className="flex items-center mb-4 text-sm">
            <input
              type="checkbox"
              className="mr-2"
              checked={rememberMe}
              onChange={e => setRememberMe(e.target.checked)}
            />
            Remember me (30 days)
          </label>

          <button
            type="submit"
            className="w-full bg-green-700 hover:bg-green-800 text-white py-2 rounded transition"
          >
            Login
          </button>
        </form>

        <p className="text-sm text-center mt-4">
          Don’t have an account?{' '}
          <a href="/register" className="text-blue-600 dark:text-blue-400 hover:underline">
            Create One
          </a>
        </p>
      </div>
    </div>
  );
}
export default LoginPage
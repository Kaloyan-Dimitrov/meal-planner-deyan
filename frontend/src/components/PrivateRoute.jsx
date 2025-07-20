import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

function isValidJwt(token) {
  if (!token || typeof token !== 'string') return false;
  const parts = token.split('.');
  return parts.length === 3 && parts.every(part => part.trim() !== '');
}

function isTokenExpired(token) {
  try {
    const { exp } = jwtDecode(token);
    return exp * 1000 < Date.now(); // true = expired
  } catch (e) {
    console.error('Failed to decode token:', e.message);
    return true; // treat as expired
  }
}

function PrivateRoute({ children }) {
  const token = localStorage.getItem('jwt');

  if (!isValidJwt(token)) {
    console.warn('Invalid JWT structure');
    return <Navigate to="/login" replace />;
  }

  if (isTokenExpired(token)) {
    console.warn('Token expired');
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default PrivateRoute;

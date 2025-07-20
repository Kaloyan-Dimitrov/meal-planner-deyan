import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import { toast } from 'react-toastify';

let hasShownToast = false;
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
    if (!hasShownToast) {
      toast.warn('Invalid session. Please log in again.');
      hasShownToast = true;
    }
    return <Navigate to="/login" replace />;
  }

  if (isTokenExpired(token)) {
    if (!hasShownToast) {
      toast.info('Session expired. Please log in again.');
      hasShownToast = true;
    }
    return <Navigate to="/login" replace />;
  }

  hasShownToast = false; // reset when access is valid
  return children
}

export default PrivateRoute;

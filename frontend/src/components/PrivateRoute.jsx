import { Navigate } from 'react-router-dom';

function PrivateRoute({ children }) {
  const token = localStorage.getItem('jwt');
  console.log('PrivateRoute token:', token);

  if (!token || token.trim() === '') {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default PrivateRoute;

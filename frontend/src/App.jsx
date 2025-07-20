import './App.css'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import { Routes } from 'react-router-dom'
import { Route } from 'react-router-dom'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import PrivateRoute from './components/PrivateRoute'
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <>
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
    path="/dashboard"
    element={
      <PrivateRoute>
        <DashboardPage />
      </PrivateRoute>
    }
  />
    </Routes>
    <ToastContainer position="top-center" autoClose={3000} />
    </>
  );
}

export default App

import './App.css'
import HomePage from './pages/HomePage'
import { Routes } from 'react-router-dom'
import { Route } from 'react-router-dom'

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
    </Routes>
  );
}

export default App

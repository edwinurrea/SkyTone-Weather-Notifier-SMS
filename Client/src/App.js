import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import FrontPage from './FrontPage.js';
import Login from './Login.js';
import SignUp from './SignUp.js';
import OTPVerfication from './OTPVerification';
import ForgotPassword from './ForgotPassword';
import ForgotOTPVerfication from './ForgotOTPVerification';
import ResetPassword from './ResetPassword';
import Dashboard from './Dashboard';

function App() {
  return (
    <div className="App">
      <Router>
        <Routes>
          <Route path="/" element={<FrontPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/verify" element={<OTPVerfication />} />
          <Route path="/forgot" element={<ForgotPassword />} />
          <Route path="/forgotOTP" element={<ForgotOTPVerfication />} />
          <Route path="/reset" element={<ResetPassword />} />
          <Route path="/dashboard" element={<Dashboard />} />    
        </Routes>
      </Router>
    </div>
  );
}

export default App;
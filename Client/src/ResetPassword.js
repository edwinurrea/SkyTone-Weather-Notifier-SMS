import React, { useState, useEffect } from 'react';
import './ResetPassword.css';
import Logo from './images/cloud-phone.png';
import { Link, useNavigate } from 'react-router-dom';

function ResetPassword() {
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const phoneNumber = sessionStorage.getItem('phoneNumber');
    if (phoneNumber) {
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      if (newPassword !== confirmNewPassword) {
        setPasswordError("Passwords do not match");
        return;
      }

      const response = await fetch('/api/resetpassword', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ password: newPassword }),
      });

      if (response.ok) {
        alert("Password reset! Logging in with new password...")
        const loginResponse = await fetch('/api/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ phoneNumber: sessionStorage.getItem('phoneNumber'), password: newPassword }),
        });
        if (loginResponse.ok) {
          const data = await loginResponse.json();
          localStorage.setItem('token', data.token);
      
          if (data.zipCodes !== null) {
          localStorage.setItem('zipCodes', JSON.stringify(data.zipCodes));
          }
    
          navigate('/dashboard'); 
        } else {
          const errorData = await loginResponse.json();
          console.error(errorData.error);
        }
      } else {
        const errorData = await response.json();
        console.error(errorData.error);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="reset-password-page">
        <div className="reset-logo-container">
        <Link to="/">
          <img src={Logo} alt="Logo" />
        </Link>
        </div>
      <h2>Reset Password</h2>
      <p>Enter a new password for SkyTone.</p>
      <form className="reset-password-form" onSubmit={handleSubmit}>
        <div className="reset-form-group">
          <label htmlFor="newPassword">New Password</label>
          <input 
            type="password" 
            id="newPassword" 
            name="newPassword" 
            placeholder="Enter your new password" 
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            style={{ borderColor: passwordError ? 'red' : 'initial' }}
          />
        </div>
        <div className="reset-form-group">
          <label htmlFor="confirmNewPassword">Confirm New Password</label>
          <input 
            type="password" 
            id="confirmNewPassword" 
            name="confirmNewPassword" 
            placeholder="Confirm your new password" 
            value={confirmNewPassword}
            onChange={(e) => setConfirmNewPassword(e.target.value)}
            style={{ borderColor: passwordError ? 'red' : 'initial' }}
          />
        {passwordError && <div className="error-message">{passwordError}</div>}
        </div>
        <button type="submit" className="reset-password-button">Reset Password</button>
      </form>
      <p>Remember your password? <Link to="/login">Log in</Link></p>
    </div>
  );
}

export default ResetPassword;

import React, { useState } from 'react';
import './OTPVerification.css';
import Logo from './images/cloud-phone.png';
import { Link, useNavigate } from 'react-router-dom';

function OTPVerification() {
  const [verificationCode, setVerificationCode] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('/api/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ verificationCode }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        navigate('/dashboard')
      } else {
        throw new Error('Network response was not ok');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="otp-verification-page">
      <div className="otp-logo-container">
        <Link to="/">
          <img src={Logo} alt="Logo" />
        </Link>
      </div>
      <h2>OTP Verification</h2>
      <p>Enter the verification code sent to your phone.</p>
      <form className="otp-verification-form" onSubmit={handleSubmit}>
        <div className="otp-form-group">
          <label htmlFor="verificationCode">Verification Code</label>
          <input
            type="text"
            id="verificationCode"
            name="verificationCode"
            placeholder="Enter the code"
            value={verificationCode}
            onChange={(e) => setVerificationCode(e.target.value)}
          />
        </div>
        <button type="submit" className="verify-button">Verify</button>
      </form>
      <p>Didn't receive the code? <Link to="/otp-verification">Resend</Link></p>
    </div>
  );
}

export default OTPVerification;

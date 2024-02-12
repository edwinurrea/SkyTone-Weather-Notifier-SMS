import React, { useState } from 'react';
import './ForgotOTPVerification.css';
import Logo from './images/cloud-phone.png';
import { Link, useNavigate } from 'react-router-dom';

function OTPVerification() {
  const [verificationCode, setVerificationCode] = useState('');
  const [resendClicked, setResendClicked] = useState(false); 
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('/api/forgotpasswordverify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ verificationCode }),
      });

      if (response.ok) {
        const data = await response.text();
        console.log(data);
        navigate('/reset');
      } else {
        const errorData = await response.json();
        console.error(errorData.error);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };
  
  const handleResend = async () => {
    try {
      const response = await fetch('/api/forgotpasswordresend', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ phoneNumber: sessionStorage.getItem('phoneNumber') }),
      });

      if (response.ok) {
        setResendClicked(true);
        console.log('Verification code resent');
      } else {
        const errorData = await response.json();
        console.error(errorData.error);
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
      <p>Didn't receive the code? {' '} {resendClicked ? (
        <span>Code resent.</span>
      ) : ( 
        <button onClick={handleResend}>Resend</button>
      )}
      </p>
    </div>
  );
}

export default OTPVerification;

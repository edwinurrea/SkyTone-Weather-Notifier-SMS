import React, { useState } from 'react';
import './ForgotPassword.css';
import Logo from './images/cloud-phone.png';
import { Link, useNavigate} from 'react-router-dom';

function ForgotPassword() {
  const [phoneNumber, setPhoneNumber] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('/api/forgotpassword', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ phoneNumber }),
      });

      if (response.ok) {
        const data = await response.text();
        sessionStorage.setItem("phoneNumber", phoneNumber)
        console.log(data);
        navigate(`/forgotOTP`);
      } else {
        const errorData = await response.json();
        console.error(errorData.error);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="forgot-password-page">
      <div className="forgot-logo-container">
        <Link to="/">
          <img src={Logo} alt="Logo" />
        </Link>
      </div>
      <h2>Forgot Password</h2>
      <p>Enter your phone number to reset your password.</p>
      <form className="forgot-password-form" onSubmit={handleSubmit}>
        <div className="forgot-form-group">
          <label htmlFor="phoneNumber">Phone Number</label>
          <input 
            type="tel" 
            id="phoneNumber" 
            name="phoneNumber" 
            placeholder="e.g. 1234567890" 
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </div>
        <button type="submit" className="forgot-password-button">Forgot Password</button>
      </form>
      <p>Remember your password? <Link to="/login">Log in</Link></p>
    </div>
  );
}

export default ForgotPassword;

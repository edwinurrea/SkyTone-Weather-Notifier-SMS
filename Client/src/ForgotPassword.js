import React, { useState } from 'react';
import './ForgotPassword.css';
import Logo from './images/cloud-phone.png';
import { Link, useNavigate} from 'react-router-dom';

function ForgotPassword() {
  const [phoneNumber, setPhoneNumber] = useState('');
  const navigate = useNavigate();

  const formatPhoneNumber = (input) => {
    const phoneNumberDigits = input.replace(/\D/g, '');
    let formattedNumber = '';
    if (phoneNumberDigits.length > 0) {
     formattedNumber += `(${phoneNumberDigits.slice(0, 3)}`;
    }
    if (phoneNumberDigits.length > 3) {
      formattedNumber += `) ${phoneNumberDigits.slice(3, 6)}`;
    }
    if (phoneNumberDigits.length > 6) {
      formattedNumber += `-${phoneNumberDigits.slice(6, 10)}`;
    }    
    return formattedNumber;
  }

  const handlePhoneNumberChange = (event) => {
    const input = event.target.value;
    const formattedInput = formatPhoneNumber(input);
    setPhoneNumber(formattedInput);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('/api/forgotpassword', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ phoneNumber: phoneNumber.replace(/\D/g, '') }),
      });

      if (response.ok) {
        const data = await response.text();
        sessionStorage.setItem("phoneNumber", phoneNumber.replace(/\D/g, ''))
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
            placeholder="(123) 456-7890" 
            value={phoneNumber}
            onChange={(handlePhoneNumberChange)}
          />
        </div>
        <button type="submit" className="forgot-password-button">Forgot Password</button>
      </form>
      <p>Remember your password? <Link to="/login">Log in</Link></p>
    </div>
  );
}

export default ForgotPassword;

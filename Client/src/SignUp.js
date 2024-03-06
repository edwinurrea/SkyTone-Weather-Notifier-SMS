import React, { useState } from 'react';
import './SignUp.css';
import Logo from './images/cloud-phone.png'; 
import { Link, useNavigate } from 'react-router-dom';

function SignUp() {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [signUpError, setSignUpError] = useState('');
  const [signUpErrorPresent, setSignUpErrorPresent] = useState(false);
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

    if (password !== confirmPassword) {
      setPasswordError('Passwords do not match.');
      return;
    }
    
    try {
      const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/api/signup`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          phoneNumber: phoneNumber.replace(/\D/g, ''), 
          password: password }),
      });

      if (response.ok) {
        const data = await response.text();
        console.log(data); 
        navigate('/verify');
      } else {
        const errorMessage = await response.json();
        setSignUpError(errorMessage.error); 
        setSignUpErrorPresent(true);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="signup-page">
      <div className="su-logo-container">
        <Link to="/">
          <img src={Logo} alt="Logo" />
        </Link>
      </div>
      <h2>Create your Account</h2>
      <form className="signup-form" onSubmit={handleSubmit}>
        <div className="su-form-group">
          <label htmlFor="phoneNumber">Phone Number</label>
          <input 
            type="tel" 
            id="phoneNumber" 
            name="phoneNumber" 
            placeholder="(123) 456-7890" 
            value={phoneNumber}
            onChange={handlePhoneNumberChange}
            style={{ borderColor: signUpErrorPresent ? 'red' : 'initial' }}
          />
          {signUpErrorPresent && <div className="error-message">{signUpError}</div>}
        </div>
        <div className="su-form-group">
          <label htmlFor="password">Password</label>
          <input 
            type="password" 
            id="password" 
            name="password" 
            placeholder="Create password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ borderColor: passwordError ? 'red' : 'initial' }}
            style2={{ borderColor: signUpErrorPresent ? 'red' : 'initial' }}
          />
        </div>
        <div className="su-form-group">
          <label htmlFor="confirmPassword">Confirm Password</label>
          <input 
            type="password"
            id="confirmPassword" 
            name="confirmPassword" 
            placeholder="Confirm password" 
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            style={{ borderColor: passwordError ? 'red' : 'initial' }}
            style2={{ borderColor: signUpErrorPresent ? 'red' : 'initial' }}
          />
        {passwordError && <div className="error-message">{passwordError}</div>}
        </div>
        <button className="su-signup-button" type="submit">Sign Up</button>
      </form>
      <p>Already have an account? <Link to="/login">Log in</Link></p>
    </div>
  );
}

export default SignUp;

import React, { useState } from 'react';
import './Login.css';
import Logo from './images/cloud-phone.png';
import eyeVisibleIcon from './images/show-password.png';
import eyeInvisibleIcon from './images/hide-password.png';
import { Link, useNavigate } from 'react-router-dom';

function Login() {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [loginError, setLoginError] = useState('');
  const [loginErrorPresent, setLoginErrorPresent] = useState(false);
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [maskedPassword, setMaskedPassword] = useState('');
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

  const handlePasswordChange = (event) => {
    const newPassword = event.target.value;
    setPassword(newPassword);
    setMaskedPassword(passwordVisible ? newPassword : newPassword);
  };

  const togglePasswordVisibility = () => {
    setPasswordVisible((prevPasswordVisible) => !prevPasswordVisible);
    setMaskedPassword((prevMaskedPassword) => {
    if (!passwordVisible) { 
      return prevMaskedPassword} 
        return password;}
    ); 
  };

  const handleLogin = async (event) => {
    event.preventDefault();

    try {
      const response = await fetch('/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          phoneNumber: phoneNumber.replace(/\D/g, ''),
          password: password
        }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('phoneNumber', phoneNumber);

        if (data.zipCodes !== null) {
          localStorage.setItem('zipCodes', JSON.stringify(data.zipCodes));
        }

        navigate('/dashboard'); 
      } else {
        setLoginError('Invalid credentials. Please try again.'); 
        setLoginErrorPresent(true);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="login-page">
      <div className="li-logo-container">
      <Link to="/">
        <img src={Logo} alt="Logo" />
      </Link>
      </div>
      <h2>Welcome Back</h2>
      <form className="login-form">
        <div className="li-form-group">
          <label htmlFor="phone">Phone Number</label>
          <input 
            type="tel" 
            id="phone" 
            name="phone" 
            placeholder="(123) 456-7890" 
            value={phoneNumber}
            onChange={handlePhoneNumberChange}
            style={{ borderColor: loginErrorPresent ? 'red' : 'initial' }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                handleLogin(e);
              }
            }}
          />
        </div>
        <div className="li-form-group">
          <label htmlFor="password">Password</label>
          <input 
            type={passwordVisible ? 'current-password' : 'password'} 
            id="password" 
            name="password" 
            placeholder="Enter your password" 
            value={maskedPassword}
            onChange={handlePasswordChange}
            style={{ borderColor: loginErrorPresent ? 'red' : 'initial' }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                handleLogin(e);
              }
            }}
          />
          <img
            src={passwordVisible ? eyeVisibleIcon : eyeInvisibleIcon}
            alt={passwordVisible ? 'visible' : 'hidden'}
            onClick={togglePasswordVisibility}
          />
          {loginErrorPresent && <div className="error-message">{loginError}</div>}
        </div>
        <div className="li-forgot-password">
          <Link to='/forgot'>
            <button className="li-forgot-password-button">Forgot Password?</button>
          </Link>
        </div>
        <button type="submit" className="li-continue-button" onClick={handleLogin}> 
          Continue
        </button>  
      </form>
      <p>Don't have an account? <a href="/signup">Sign Up</a></p>
    </div>
  );
}

export default Login;
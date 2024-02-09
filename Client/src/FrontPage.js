import React from 'react';
import './FrontPage.css';
import Logo from './images/cloud-phone.png';
import { useNavigate, Link } from 'react-router-dom';

function FrontPage() {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    console.log('Login button clicked');
    navigate('/login');
  };
  
  const handleSignUpClick = () => {
    console.log('Sign Up button clicked');
    navigate('/signup');
  };
  
  return (
    <div className="fp-login-container">
      <div className="fp-inner-container">
        <div className="fp-logo-container">
        <Link to="/">
          <img src={Logo} alt="Logo" style={{maxWidth: '140px', maxHeight: '120px'}} />
        </Link>
        </div>
        <h1>Welcome to SkyTone!</h1>
          <p>Where Every Tone Keeps You Prepared and Informed!</p> 
          <p>Log in or Sign up to Elevate Your Weather Awareness.</p>
        <div className="fp-button-container">
          <button className="fp-login-button" onClick={handleLoginClick}>Login</button>
          <button className="fp-signup-button" onClick={handleSignUpClick}>Sign Up</button>
        </div>
      </div>
    </div>
  );
}

export default FrontPage;
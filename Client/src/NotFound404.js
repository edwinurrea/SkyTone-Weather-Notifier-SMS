import React from 'react';
import './NotFound404.css';
import Logo from './images/cloud-phone.png';
import SadFace from './images/sad-face.png';
import { useNavigate, Link } from 'react-router-dom';

function NotFound404() {
  const navigate = useNavigate();

	const handleReturn = () => {
		console.log('Return button clicked.');
		navigate('./');
	}

  return (
  	<div className="notfound">
    	<div className="notfound-logo-container">
        <Link to='/'>
          <img src={Logo} alt="Logo" style={{maxWidth: '120px', maxHeight: '100px'}}/>
        </Link>
      </div>
			<div className="sad-face-404">
				<img src={SadFace} alt="404"/>
			</div>
      <div className="not-found-description">
        <h1>Oops! Page Not Found!</h1>
        <p>It looks like this page doesn't exist or isn't available right now.</p> 
      	<p>Check the URL for any typos or return to the front page!</p>
				<div>
					<button className="return-front-button" onClick={handleReturn}>Return To Front Page</button>
				</div>
      </div>
  	</div>
  );
}

export default NotFound404;
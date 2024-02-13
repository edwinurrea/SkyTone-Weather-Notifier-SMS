import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import Logo from './images/cloud-phone.png';
import Pencil from './images/newpencil.png';
import Trash from './images/trashcan.png';
import { Link, useNavigate } from 'react-router-dom';

function Dashboard() {
  const [zipCodes, setZipCodes] = useState([]);
  const [newZipCode, setNewZipCode] = useState('');
  const [newDeliveryTime, setNewDeliveryTime] = useState('12:00');
  const [weatherData, setWeatherData] = useState([]);
  const [addError, setAddError] = useState({ zipCode: false, deliveryTime: false });
  const [editIndex, setEditIndex] = useState(false);
  const navigate = useNavigate();  

  const handleZipCodeChange = (event) => {
    setNewZipCode(event.target.value);
    setAddError({ ...addError, zipCode: true});
  };

  const handleDeliveryTimeChange = (event) => {
    setNewDeliveryTime(event.target.value);
    setAddError({ ...addError, deliveryTime: true});
  };

  const handleEditZipCode = (index) => {
    setEditIndex(index);
  };

  const handleDeliveryTimeEditChange = (event) => {
    const editedZipCodes = [...zipCodes];
    editedZipCodes[editIndex].deliveryTime = event.target.value;
    setZipCodes(editedZipCodes);
  };
  
  const handleEditCancel = () => {
    const index = editIndex;

    if(index !== null) {
      const editedZipCodes = [...zipCodes];
      setZipCodes(editedZipCodes);
      setEditIndex(false);
    }
  };

  const handleEditConfirm = async () => {
    try {
      const token = localStorage.getItem('token');
      const zipCodeToEdit = zipCodes[editIndex].zipCode;
      const oldDeliveryTime = zipCodes[editIndex].deliveryTime;
      const editedDeliveryTime = zipCodes[editIndex].deliveryTime;

      const response = await fetch('/api/editZipCode', {
        method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
              zipCode: zipCodeToEdit,
              oldDeliveryTime: oldDeliveryTime,
              newDeliveryTime: editedDeliveryTime
            }),
          });
  
      if (response.ok) {
        const updatedZipCodes = [...zipCodes];
        updatedZipCodes[editIndex].deliveryTime = editedDeliveryTime;
        setZipCodes(updatedZipCodes);
        console.log('Zip code and delivery time updated successfully');
      } else {
        console.error('Failed to update zip code and delivery time:', response.statusText);
      }

      setEditIndex(null);
    } catch (error) {
      console.error('An error occurred:', error);
    }
  };

  const handleDeleteZipCode = async (index) => {
    try {
      const token = localStorage.getItem('token');
      const zipCodeToDelete = zipCodes[index].zipCode;
      const deliveryTimeToDelete = zipCodes[index].deliveryTime;
  
      const response = await fetch('/api/deleteZipCode', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ zipCode: zipCodeToDelete, deliveryTime: deliveryTimeToDelete }),
      });
  
      if (response.ok) {
        const updatedZipCodes = [...zipCodes];
        updatedZipCodes.splice(index, 1);
        setZipCodes(updatedZipCodes);
        console.log('Zip code and delivery time deleted successfully');
      } else {
        console.error('Failed to delete zip code and delivery time:', response.statusText);
      }
    } catch (error) {
      console.error('An error occurred:', error);
    }
  };

  const addZipCode = async (event) => {
    event.preventDefault();

    if (!newZipCode && !newDeliveryTime) {
      setAddError({ zipCode: true, deliveryTime: true });
      alert('Zip code and delivery time are both blank. Please fill in the fields.');
        return;
    }

    if (!newZipCode) {
      setAddError({ zipCode: true, deliveryTime: false });
      alert('Zip code is blank. Please fill in the zip code field.');
        return;
    }

    if (!newDeliveryTime) {
      setAddError({ zipCode: false, deliveryTime: true });
      alert('Delivery time is blank. Please fill in the delivery time field.');
        return;
    }

    setAddError({ zipCode: false, deliveryTime: false });

    const newZipCodeData = { zipCode: newZipCode, deliveryTime: newDeliveryTime };
    const isDuplicate = zipCodes.some(data => data.zipCode === newZipCodeData.zipCode && data.deliveryTime === newZipCodeData.deliveryTime);
    
    if (isDuplicate) {
      alert('This zip code and delivery time combination already exists.'); 
      return;
    }

    setZipCodes([...zipCodes, newZipCodeData]);
    setNewZipCode('');
    setNewDeliveryTime('12:00');
    console.log(newZipCodeData);

    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/addZipCode', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(newZipCodeData),
      });

      if (response.ok) {
        const storedZipCodes = JSON.parse(localStorage.getItem('zipCodes'));
        const updatedStoredZipCodes = [...storedZipCodes, newZipCodeData];
        const originalZipCodes = [...new Set(storedZipCodes.map(data => data.zipCode))];
        const uniqueZipCodes = [...new Set(updatedStoredZipCodes.map(data => data.zipCode))];
        
        if (JSON.stringify(uniqueZipCodes) === JSON.stringify(originalZipCodes)) {
          return;
        } else {

        const zipCodesQueryParam = `zipCodes=${newZipCodeData.zipCode}`;
        console.log(zipCodesQueryParam)
        fetch(`/api/weather?${zipCodesQueryParam}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
          .then(response => response.json())
          .then(data => {
            console.log("Received Data:", data);

            const newWeatherData = data.map(weatherInfo => ({
              locationName: weatherInfo.locationName,
              zipCode: weatherInfo.zipCode,
              date: weatherInfo.date,
              maxTemperature: weatherInfo.maxTemperature,
              minTemperature: weatherInfo.minTemperature,
              weatherCondition: weatherInfo.weatherCondition,
              chanceOfRain: weatherInfo.chanceOfRain,
              windSpeed: weatherInfo.windSpeed,
              windDirection: weatherInfo.windDirection,
              sunriseTime: weatherInfo.sunriseTime,
              sunsetTime: weatherInfo.sunsetTime,
            }));
            setWeatherData(prevWeatherData => [...prevWeatherData, ...newWeatherData]);
          })
          .catch(error => console.error('Error:', error));
        }
      } else {
      }  
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const getLocationNameByZipCode = (zipCode) => {
    const matchingWeatherData = weatherData.find(data => data.zipCode === zipCode);
    if (matchingWeatherData) {
      return matchingWeatherData.locationName;
    } else {
      const otherZipCodes = zipCodes.filter(data => data.zipCode !== zipCode);

      const closestZipCode = otherZipCodes.reduce((closest, current) => {
          const closestDistance = Math.abs(parseInt(closest.zipCode, 10) - parseInt(zipCode, 10));
          const currentDistance = Math.abs(parseInt(current.zipCode, 10) - parseInt(zipCode, 10));
          return currentDistance < closestDistance ? current : closest;
        }, zipCodes[0]);
      const closestWeatherData = weatherData.find(data => data.zipCode === closestZipCode.zipCode);  
        
      return closestWeatherData ? closestWeatherData.locationName : "N/A";
    }
  };

  const formattedDate = (dateString) => {
    const options = { weekday: 'long', month: 'long', day: 'numeric' };
    const formattedDate = new Date(dateString).toLocaleDateString(undefined, options);
    return formattedDate;
  };

  const formatTimeForDisplay = (timeString) => {
    const options = { hour: 'numeric', minute: 'numeric', hour12: true };
    return new Date(`2000-01-01T${timeString}`).toLocaleTimeString(undefined, options);
  };  

  useEffect(() => {
    console.log('Dashboard component mounted');
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/');
      localStorage.clear();
    } else {
      const storedZipCodesString = localStorage.getItem('zipCodes');
      if (!storedZipCodesString) {
      return;
    } else {
      const storedZipCodes = JSON.parse(storedZipCodesString);
      console.log("Stored Zip Codes:", storedZipCodes);
      setZipCodes([...storedZipCodes]);

      const uniqueZipCodes = [...new Set(storedZipCodes.map(data => data.zipCode))];
      const zipCodesQueryParam = uniqueZipCodes.map(uniqueZipCode => `zipCodes=${uniqueZipCode}`).join('&');
      console.log("Zip Codes Query Param:", zipCodesQueryParam);

      if (uniqueZipCodes.length > 0) {
        fetch(`/api/weather?${zipCodesQueryParam}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
        .then(response => response.json())
          .then(data => {
            console.log("Received Data:", data);

            const newWeatherData = data.map(weatherInfo => ({
              locationName: weatherInfo.locationName,
              zipCode: weatherInfo.zipCode,
              date: weatherInfo.date,
              maxTemperature: weatherInfo.maxTemperature,
              minTemperature: weatherInfo.minTemperature,
              weatherCondition: weatherInfo.weatherCondition,
              chanceOfRain: weatherInfo.chanceOfRain,
              windSpeed: weatherInfo.windSpeed,
              windDirection: weatherInfo.windDirection,
              sunriseTime: weatherInfo.sunriseTime,
              sunsetTime: weatherInfo.sunsetTime,
            }));
          setWeatherData(newWeatherData);
        })
        .catch(error => console.error('Error:', error)); 
      }
    }}
      window.addEventListener('beforeunload', () => {
        localStorage.removeItem('token');
        localStorage.clear();
      });
    }, [navigate]);  

    const handleLogout = () => {
      localStorage.clear();
      navigate('/');
    };

  return (
    <div className="dashboard">
      <div className="dash-logo-container">
      <Link to="/">
        <img src={Logo} alt="Logo" />
      </Link>
      </div>
      <h2>Welcome to Your Dashboard</h2>
      <div className="weather-section">
      <div className="preview-indicator">Live Preview</div>
      <div className="weather-messages">
        {weatherData.length > 0 ? (
          (() => {
            const weatherByZipCode = {};

            weatherData.forEach(data => {
              if (!weatherByZipCode[data.zipCode]) {
                weatherByZipCode[data.zipCode] = [];
              }
              weatherByZipCode[data.zipCode].push(data);
            });
              
            return Object.keys(weatherByZipCode).map((uniqueZipCode, index) => (
              <div key={index}>
                {weatherByZipCode[uniqueZipCode].map((data, innerIndex) => (
                  <div className="weather-message" key={innerIndex}>
                    <p>{data.locationName}</p>
                    <p>{formattedDate(data.date)}</p>
                    <hr />
                    <p>Condition: {data.weatherCondition}</p>
                    <hr />
                    <p>Temperature:</p>
                    <p>High: {data.maxTemperature}°F</p>
                    <p>Low: {data.minTemperature}°F</p>
                    <hr /> 
                    <p>Additional Information:</p>
                    <p>Chance of Rain: {data.chanceOfRain}%</p>
                    <p>Wind: {data.windSpeed} mph, {data.windDirection}</p>
                    <p>Sunrise: {data.sunriseTime}</p>
                    <p>Sunset: {data.sunsetTime}</p>
                  </div>
                ))}
              </div>
            ));
          })()
        ) : (
          <div>Loading weather data...</div>
        )}
      </div>
      </div>
      <div className="zip-code-section">
        <h3>Manage Zip Codes and Delivery Times</h3>
        <div className="zip-code-form">
          <div>
            <input
              type="text"
              id="zipcode"
              placeholder="Enter Zip Code"
              value={newZipCode}
              onChange={handleZipCodeChange}
              style={{ borderColor: (addError.zipCode && ((!newZipCode && !newDeliveryTime) || !newZipCode)) ? 'red' : 'initial' }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  addZipCode(e);
                }
              }}
            />
            {addError.zipCode && ((!newZipCode && !newDeliveryTime) || !newZipCode) && <div className="error-message">Zip code required</div>}
          </div>
          <div>
            <input
              type="time"
              id="deliverytime"
              value={newDeliveryTime}
              onChange={handleDeliveryTimeChange}
              style={{ borderColor: (addError.deliveryTime && ((!newZipCode && !newDeliveryTime) || !newDeliveryTime)) ? 'red' : 'initial' }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  addZipCode(e);
                }
              }}
            />
            {addError.deliveryTime && ((!newZipCode && !newDeliveryTime) || !newDeliveryTime) && <div className="error-message">Delivery time required</div>}
          </div>
          <button onClick={addZipCode}>Add</button>
        </div>
        <div className="zip-codes-list">
          <table>
            <thead>
              <tr>
                <th>Location Name</th>
                <th>Zip Code</th>
                <th>Delivery Time</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {zipCodes.map((zipCode, index) => (
                <tr key={index}>
                  <td>{getLocationNameByZipCode(zipCode.zipCode)}</td>
                  <td>{zipCode.zipCode}</td>
                  <td>
                    {editIndex === index ? (
                      <input
                        type="time"
                        value={zipCode.deliveryTime}
                        onChange={handleDeliveryTimeEditChange}
                      />
                    ) : (
                      formatTimeForDisplay(zipCode.deliveryTime)
                    )}
                  </td>
                  <td>
                    {editIndex === index ? (
                      <>
                        <button onClick={handleEditCancel}>Cancel</button>
                        <button onClick={handleEditConfirm}>Confirm</button>
                      </>
                    ) : (
                      <>
                        <div className="edit-delete-container">
                          <img
                            src={Pencil}
                            alt="Edit"
                            className="edit-icon"
                            onClick={() => handleEditZipCode(index)}
                          />
                          <img
                            src={Trash} 
                            alt="Delete"
                            className="delete-icon"
                            onClick={() => handleDeleteZipCode(index)}
                          /> 
                        </div>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="logout-button">
          <button onClick={handleLogout}>Logout</button>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;

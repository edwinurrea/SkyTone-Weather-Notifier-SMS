package com.edwinurrea.weathernotifier;

import java.sql.SQLException;

public class SubscriptionManager extends WeatherNotifier {
    protected SubscriberDao subscriberDao;
    
    public SubscriptionManager(SubscriberDao subscriberDao) {
        this.subscriberDao = subscriberDao;
    }
    
    public void subscribeUser(int userId) throws SQLException {
        SubscriberDao.insertSubscriber(userId);
    }
}

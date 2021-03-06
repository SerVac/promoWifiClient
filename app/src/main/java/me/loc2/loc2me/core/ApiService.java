
package me.loc2.loc2me.core;

import com.google.common.collect.ImmutableList;

import java.util.List;

import me.loc2.loc2me.core.models.Offer;
import me.loc2.loc2me.core.rest.WifiOffersService;
import retrofit.RestAdapter;

/**
 * API service
 */
public class ApiService {

    private RestAdapter restAdapter;

    private WifiOffersService wifiOffersService;

    /**
     * Create bootstrap service
     *
     * @param restAdapter The RestAdapter that allows HTTP Communication.
     */
    public ApiService(RestAdapter restAdapter) {
        this.restAdapter = restAdapter;
    }


    private WifiOffersService getWifiOfferService() {
        if (null == wifiOffersService) {
            wifiOffersService = getRestAdapter().create(WifiOffersService.class);
        }
        return wifiOffersService;
    }

    private RestAdapter getRestAdapter() {
        return restAdapter;
    }

    public List<Offer> getWifiOffers(String name, ImmutableList<String> filters) {
        List<Offer> wifiOffers = getWifiOfferService().getWifiOffers(name, filters);
        return wifiOffers;
    }

}
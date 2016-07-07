package co.thnki.locationalarm.interfaces;

public interface GeoCodeListener
{
    void onAddressObtained(String result);
    void onGeoCodingFailed();
    void onCancelled();
}

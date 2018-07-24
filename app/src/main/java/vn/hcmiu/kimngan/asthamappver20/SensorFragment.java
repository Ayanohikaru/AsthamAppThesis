package vn.hcmiu.kimngan.asthamappver20;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SensorFragment extends Fragment {
    private BandClient client = null;
    private TextView RR_value;
    private TextView HR_value;
    private TextView Access_value;

    private BandRRIntervalEventListener mRRIntervalEventListener = new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
            if (event != null) {
                RR_appendToUI(String.format(Locale.US, "RR Interval = %.3f s\n", event.getInterval()));
            }
        }
    };

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                HR_appendToUI(String.format("Heart Rate = %d beats per minute\n"
                        + "Quality = %s\n", event.getHeartRate(), event.getQuality()));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        String myValue = this.getArguments().getString("message");
        return inflater.inflate(R.layout.fragment_sensor, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstantState) {
        super.onActivityCreated(savedInstantState);

        Button btn_Consent = (Button) getView().findViewById(R.id.btn_Consent);
        Button btn_Measure = (Button) getView().findViewById(R.id.btn_Measure);
        RR_value = (TextView) getView().findViewById(R.id.RR_value);
        HR_value = (TextView) getView().findViewById(R.id.HR_value);

        btn_Measure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RR_value.setText("");
                HR_value.setText("");
                new RRIntervalSubscriptionTask().execute();
                new HeartRateSubscriptionTask().execute();
            }
        });
        final WeakReference<Activity> reference = new WeakReference<Activity>(getActivity());
        final WeakReference<Activity> reference1 = new WeakReference<Activity>(getActivity());
        btn_Consent.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);
                new HeartRateConsentTask().execute(reference1);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        RR_value.setText("");
        HR_value.setText("");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (client != null) {
            try {
                client.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
            } catch (BandIOException e) {
                RR_appendToUI(e.getMessage());
            }
            try {
                client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
            } catch (BandIOException e) {
                RR_appendToUI(e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException | BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }

    private class RRIntervalSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
                    if (hardwareVersion >= 20) {
                        if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                            client.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
                        } else {
                            RR_appendToUI("You have not given this application consent to access heart rate data yet."
                                    + " Please press the Heart Rate Consent button.\n");
                        }
                    } else {
                        RR_appendToUI("The RR Interval sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
                    }
                } else {
                    RR_appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                RR_appendToUI(exceptionMessage);

            } catch (Exception e) {
                RR_appendToUI(e.getMessage());
            }
            return null;
        }
    }

    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    } else {
                        HR_appendToUI("You have not given this application consent to access heart rate data yet."
                                + " Please press the Heart Rate Consent button.\n");
                    }
                } else {
                    HR_appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                HR_appendToUI(exceptionMessage);

            } catch (Exception e) {
                HR_appendToUI(e.getMessage());
            }
            return null;
        }
    }
    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                            }
                        });
                    }
                } else {
                    HR_appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                HR_appendToUI(exceptionMessage);

            } catch (Exception e) {
                HR_appendToUI(e.getMessage());
            }
            return null;
        }
    }

    private void RR_appendToUI(final String string) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RR_value.setText(string);
            }
        });
    }

    private void HR_appendToUI(final String string) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HR_value.setText(string);
            }
        });
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                RR_appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getActivity().getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        RR_appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}

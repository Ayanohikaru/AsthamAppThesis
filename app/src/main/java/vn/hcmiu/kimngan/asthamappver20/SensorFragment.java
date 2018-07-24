package vn.hcmiu.kimngan.asthamappver20;


import android.app.Activity;
import android.hardware.SensorEventListener;
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
import com.microsoft.band.sensors.HeartRateQuality;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SensorFragment extends Fragment {
    private BandClient client = null;
    private TextView RR_value;
    private TextView HR_value;
    private TextView Access_value;
    private TextView Sum_value;

    private double rrTemp;
    private HeartRateQuality qualityTemp;
    private List<Double> rrList = new ArrayList<>();
    private double rrSum;

    private BandRRIntervalEventListener mRRIntervalEventListener = new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
            if (event != null) {
                //RR_appendToUI(String.format(Locale.US, "RR Interval = %.3f s\n", event.getInterval()));
                rrTemp = event.getInterval();
            }
        }
    };

    public Double sum(List<Double> list) {
        double sum = 0;
        for (Double i : list)
            sum = sum + i;
        return sum;
    }

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                Access_appendToUI(String.format("Quality = %s\n",event.getQuality()));
                qualityTemp = event.getQuality();
                if(qualityTemp == HeartRateQuality.LOCKED){
                    HR_appendToUI(String.format("Heart Rate = %d beats per minute\n",event.getHeartRate()));
                    RR_appendToUI(String.format(Locale.US, "RR Interval = %.3f s\n", rrTemp));
                    rrList.add(rrTemp);
                    if(rrList.size()==60){
                        rrSum = sum(rrList);
                        rrSum = rrSum/60;
                        Sum_appendToUI(String.format(Locale.US, "Sum = %.3f s\n", rrSum));
                       // Sum_value.setText(String.valueOf(rrSum));
                        rrList.clear();
                        rrSum = 0;
                    }
                }
                else
                {
                    HR_appendToUI("Not wearing band");
                    RR_appendToUI("Not wearing band");;
                }

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
        Access_value = (TextView)getView().findViewById(R.id.Access_value);
        Sum_value = (TextView)getView().findViewById(R.id.rrSum_value);

        btn_Measure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RR_value.setText("");
                HR_value.setText("");
                //new RRIntervalSubscriptionTask().execute();
                new HeartRateSubscriptionTask().execute();
            }
        });
        final WeakReference<Activity> reference = new WeakReference<Activity>(getActivity());
        btn_Consent.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);
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

    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                        client.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
                    } else {
                        Access_appendToUI("You have not given this application consent to access heart rate data yet."
                                + " Please press the Heart Rate Consent button.\n");
                    }
                } else {
                    Access_appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
                Access_appendToUI(exceptionMessage);

            } catch (Exception e) {
                Access_appendToUI(e.getMessage());
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
                    Access_appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
                Access_appendToUI(exceptionMessage);

            } catch (Exception e) {
                Access_appendToUI(e.getMessage());
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
    private void Sum_appendToUI(final String string) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Sum_value.setText(string);
            }
        });
    }
    private void Access_appendToUI(final String string) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Access_value.setText(string);
            }
        });
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                Access_appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getActivity().getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        Access_appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}

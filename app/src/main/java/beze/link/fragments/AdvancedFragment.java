package beze.link.fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import beze.link.Globals;
import com.android.beze.link.R;
import beze.link.obd2.Protocols;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdvancedFragment extends Fragment implements View.OnClickListener {


    public AdvancedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_advanced, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Button send = getActivity().findViewById(R.id.buttonSend);
        send.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        EditText sendText = getActivity().findViewById(R.id.editTextAdvancedSend);
        TextView responseText = getActivity().findViewById(R.id.textViewElmResponse);
        String data = sendText.getText().toString();

        if (Globals.cable != null && Globals.cable.IsOpen()) {

            Globals.cable.Send(data);
            String response = Globals.cable.Receive(10000);

            if (response != null && !response.isEmpty()) {
                response = response.replace(Protocols.Elm327.EndOfLine, "\r\n");

                responseText.setText(response);
                responseText.invalidate();
            }
            else {
                Toast.makeText(Globals.appContext, "Null or empty response! Sent: " + data, Toast.LENGTH_LONG);
            }
        }
        else {
            Toast.makeText(Globals.appContext, "Cable is not connected!", Toast.LENGTH_LONG);
        }
    }

}

package co.edu.itm.pipelosses;


import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;


import android.webkit.WebView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;
import android.text.TextWatcher;
import android.text.Editable;

import java.text.NumberFormat;


public class MainActivity extends AppCompatActivity {

    public String flowRegime = "Laminar";
    public String tubeMaterial = "Steel";
    public String selectedFluid = "Water";
    public double fluidKinematicViscosity = 1.02e-6;//m2/s, water at 20°
    public double fluidDensity = 997;//kg/m3 at 20°C
    public double Diameter = 1;//m
    public double sectionArea = 1;//m2
    public double meanVelocity = 1;//m/s
    public double volumetricFlow = 1;//m3/s
    public double volumetricFlowGPM = 1;//gpm
    public double volumetricFlowLPM = 1;//lpm
    public double massFlow = 1;//kg/s
    public double Reynolds = 1;//non-dimensional
    public double frictionFactor = 1;//non-dimensional
    public double roughness = 0;//mm
    public double tubeLength = 0;//m
    public double majorLosses = 0;//m
    public double minorLosses = 0;//m
    public double totalLosses = 0;//m
    public double totalLosseskW = 0;//kW
    public double totalLossesHP = 0;//kW
    public double SumOfLDequivalent = 0;//m
    public int flowRegimeIndex = 0;



    private static final NumberFormat tempFormat = NumberFormat.getInstance();
    private static final NumberFormat lengthFormat = NumberFormat.getInstance();
    private static final NumberFormat frictionFormat = NumberFormat.getInstance();
    private static final NumberFormat viscosityFormat = NumberFormat.getInstance();



    //---------Results data text views------------
    private TextView volumetricFlowGPMDataTextView;
    private TextView volumetricFlowLPMDataTextView;
    private TextView meanVelocityDataTextView;
    private TextView ReynoldsDataTextView;
    private TextView flowRegimeDataTextView;
    private TextView frictionFactorDataTextView;
    private TextView fluidKinematicViscosityDataTextView;
    private TextView fluidDensityDataTextView;
    private TextView massFlowDataTextView;

    private TextView fullAccessoriesWorkingListTextView;
    private TextView minorLossesValuesTextView;
    private TextView majorLossesValuesTextView;
    private TextView totalLossesValuesTextView;
    private TextView totalLosseskWValuesTextView;
    private TextView totalLossesHPValuesTextView;

    private EditText volumetricFlowDataEditText;
    private EditText numberOfAccessoriesDataEditText;
    private EditText tubeLengthsEditText;





    public void updateCalculation() {


        //----------Test for checking if tubeLengthsEditText is empty--------------

        String testString0 = tubeLengthsEditText.getText().toString();
        boolean test0 = "".equals(testString0);

        if (test0)
        {
            tubeLength = 0;
        }
        else
        {
            String testString3 = NumUtils.makeToDouble(testString0.substring(0, testString0.length() - 2));
            tubeLength = Double.parseDouble(testString3);
        }



        //----------Test for checking if volumetricFlowDataEditText is empty--------------

       // volumetricFlowDataEditText.setText(volumetricFlowDataEditText.getText().toString())-"m";

        String testString = volumetricFlowDataEditText.getText().toString();
        boolean test1 = "".equals(testString);

        if (test1)
        {
            volumetricFlow = 0;
        }
        else
        {
                String testString2 = NumUtils.makeToDouble(testString.substring(0, testString.length() - 5));
                volumetricFlow = Double.parseDouble(testString2);
                volumetricFlowGPM = volumetricFlow*15850.3;//from m3/s to gal/min
                volumetricFlowLPM = volumetricFlow*60000;//from m3/s to l/min
                massFlow = fluidDensity*volumetricFlow;
        }

        sectionArea = Math.PI * Diameter * Diameter / 4;
        meanVelocity = volumetricFlow / sectionArea;
        Reynolds = Diameter * meanVelocity / fluidKinematicViscosity;


//------ Flow regime calculation---------

        if (Reynolds < 2300)
        {
            flowRegime = "Laminar";
            flowRegimeIndex = 0;
        }
        else
        {

            if (Reynolds < 10000)
            {
                flowRegime = "Transition";
                flowRegimeIndex = 1;
            }
            else
            {
                flowRegime = "Turbulent";
                flowRegimeIndex = 2;
            }

        }

        //------- friction factor calculation ---------

        if (flowRegime == "Laminar")
        {
                frictionFactor = 64 / Reynolds;
        }
        else //turbulent or transition
        {
            if (((tubeMaterial == "smoothTube") || (tubeMaterial == "glass")) || (tubeMaterial == "plastic"))
            {
                frictionFactor = Math.pow(0.79 * Math.log(Reynolds) - 1.64, -2);
            }
            else
            {
                frictionFactor = Math.pow(1 / (-1.8 * Math.log10(6.9 / Reynolds + Math.pow(roughness / (3.7 * Diameter), 1.11))), 2);
            }
        }


        //--------- Losses ----------------------

        majorLosses = frictionFactor*(tubeLength/Diameter)*(meanVelocity*meanVelocity)/(2*9.81);

        minorLosses = frictionFactor*(SumOfLDequivalent)*(meanVelocity*meanVelocity)/(2*9.81);

        totalLosses = majorLosses + minorLosses;//in m
        totalLosseskW = totalLosses*massFlow*9.81/1000;//in kW
        totalLossesHP = totalLosseskW*1.34102;//in HP


        String[] stringArray = getResources().getStringArray(R.array.flowRegime);
        String flowRegimeLabels = stringArray[flowRegimeIndex];


        //Results update

        volumetricFlowGPMDataTextView.setText(frictionFormat.format(volumetricFlowGPM) + " GPM");
        volumetricFlowLPMDataTextView.setText(frictionFormat.format(volumetricFlowLPM) + " LPM");
        meanVelocityDataTextView.setText(frictionFormat.format(meanVelocity) + " m/s");
        ReynoldsDataTextView.setText(tempFormat.format(Reynolds));
        flowRegimeDataTextView.setText(flowRegimeLabels);
        frictionFactorDataTextView.setText(frictionFormat.format(frictionFactor));
        fluidKinematicViscosityDataTextView.setText(viscosityFormat.format(fluidKinematicViscosity)+ " m²/s");
        fluidDensityDataTextView.setText(viscosityFormat.format(fluidDensity)+ " kg/m³");
        massFlowDataTextView.setText(viscosityFormat.format(massFlow)+ " kg/s");
        minorLossesValuesTextView.setText(frictionFormat.format(minorLosses)+ " m");
        majorLossesValuesTextView.setText(frictionFormat.format(majorLosses)+ " m");
        totalLossesValuesTextView.setText(frictionFormat.format(totalLosses)+ " m");
        totalLosseskWValuesTextView.setText(frictionFormat.format(totalLosseskW)+ " kW");
        totalLossesHPValuesTextView.setText(frictionFormat.format(totalLossesHP)+ " HP");
    }







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tempFormat.setMinimumFractionDigits(0);
        tempFormat.setMaximumFractionDigits(2);

        lengthFormat.setMinimumFractionDigits(0);
        lengthFormat.setMaximumFractionDigits(2);

        frictionFormat.setMinimumFractionDigits(0);
        frictionFormat.setMaximumFractionDigits(4);

        viscosityFormat.setMinimumFractionDigits(0);
        viscosityFormat.setMaximumFractionDigits(7);


        //Spinner fluido
        Spinner spinnerFluid = (Spinner) findViewById(R.id.spinnerFluido);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Fluids, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFluid.setAdapter(adapter);

        //Spinner tube diameter
        final Spinner spinnerDiam = (Spinner) findViewById(R.id.diameterSpinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.Diameter, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiam.setAdapter(adapter2);

        //spinnerMaterial
        Spinner spinnerTubeMaterial = (Spinner) findViewById(R.id.spinnerMaterial);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.MaterialList, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTubeMaterial.setAdapter(adapter3);

        //spinnerAccessories
        final Spinner spinnerTubeAccessories = (Spinner) findViewById(R.id.spinnerAccessories);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this, R.array.AccessorieslList, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTubeAccessories.setAdapter(adapter4);


        //---Results textview declaration-----
        volumetricFlowDataEditText = (EditText) findViewById(R.id.volumetricFlowEditText);
        numberOfAccessoriesDataEditText = (EditText) findViewById(R.id.numberOfAccessoriesEditText);
        tubeLengthsEditText = (EditText) findViewById(R.id.tubeLengthEditText);

        volumetricFlowGPMDataTextView = (TextView) findViewById(R.id.volumetricFlowGPMTextView);
        volumetricFlowLPMDataTextView = (TextView) findViewById(R.id.volumetricFlowLPMTextView);

        meanVelocityDataTextView = (TextView) findViewById(R.id.meanVelocityTextView);
        ReynoldsDataTextView = (TextView) findViewById(R.id.ReynoldsTextView);
        flowRegimeDataTextView = (TextView) findViewById(R.id.flowRegimeTextView);
        frictionFactorDataTextView = (TextView) findViewById(R.id.frictionFactorTextView);
        fluidKinematicViscosityDataTextView = (TextView) findViewById(R.id.fluidKinematicViscosityTextView);
        fluidDensityDataTextView = (TextView) findViewById(R.id.fluidDensityTextView);
        massFlowDataTextView = (TextView) findViewById(R.id.massFlowTextView);
        fullAccessoriesWorkingListTextView = (TextView) findViewById(R.id.fullAccessoriesListTextView);
        minorLossesValuesTextView = (TextView) findViewById(R.id.minorLossesValueTextView);
        majorLossesValuesTextView = (TextView) findViewById(R.id.majorLossesValueTextView);
        totalLossesValuesTextView = (TextView) findViewById(R.id.totalLossesValueTextView);
        totalLosseskWValuesTextView = (TextView) findViewById(R.id.totalLosseskWValueTextView);
        totalLossesHPValuesTextView = (TextView) findViewById(R.id.totalLossesHPValueTextView);

        updateCalculation();


        Button addButtons = (Button) findViewById(R.id.addButton);
        addButtons.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Routine for printing accessories added
                fullAccessoriesWorkingListTextView.setText( fullAccessoriesWorkingListTextView.getText()+ "\n" + numberOfAccessoriesDataEditText.getText() + " x " + spinnerTubeAccessories.getSelectedItem().toString());

                String[] stringArray = getResources().getStringArray(R.array.LDequivalentList);
                int indexOfArray = (int) spinnerTubeAccessories.getSelectedItemId();

                double numberOfAcc  = Double.parseDouble(numberOfAccessoriesDataEditText.getText().toString());
                SumOfLDequivalent = SumOfLDequivalent + Double.parseDouble(stringArray[indexOfArray])*numberOfAcc;

                updateCalculation();
                //minorLossesValuesTextView.setText(frictionFormat.format(SumOfLDequivalent));
                //fullAccessoriesWorkingListTextView.setText(frictionFormat.format(SumOfLDequivalent));

            }
        });


        Button eraseButtons = (Button) findViewById(R.id.eraseButton);
        eraseButtons.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                fullAccessoriesWorkingListTextView.setText("");
                SumOfLDequivalent = 0;
                updateCalculation();
            }
        });




        tubeLengthsEditText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable e) {


                String s = e.toString();
                if (s.length() > 0) {
                    if (s.startsWith("."))
                    {
                        tubeLengthsEditText.setText("0"+ s);
                    }

                    if (!s.endsWith(" m")) {
                        if (!s.equals(s + " m")) {
                            s = s.replaceAll("[^\\d.]", "");
                            tubeLengthsEditText.setText(s + " m");
                        } else {
                            tubeLengthsEditText.setSelection(s.length() - " m".length());
                        }
                    } else {
                        tubeLengthsEditText.setSelection(s.length() - " m".length());
                        if (s.equals(" m")) {
                            tubeLengthsEditText.setText("");
                        }
                    }
                }

                updateCalculation();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });





        volumetricFlowDataEditText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable e) {


                String s = e.toString();
                if (s.length() > 0) {
                    if (s.startsWith("."))
                    {
                        volumetricFlowDataEditText.setText("0"+ s);
                    }

                    if (!s.endsWith(" m³/s")) {
                        if (!s.equals(s + " m³/s")) {
                            s = s.replaceAll("[^\\d.]", "");
                            volumetricFlowDataEditText.setText(s + " m³/s");
                        } else {
                            volumetricFlowDataEditText.setSelection(s.length() - " m³/s".length());
                        }
                    } else {
                        volumetricFlowDataEditText.setSelection(s.length() - " m³/s".length());
                        if (s.equals(" m³/s")) {
                            volumetricFlowDataEditText.setText("");
                        }
                    }
                }

                 updateCalculation();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        spinnerTubeMaterial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                if (position == 0)
                {
                    tubeMaterial = "smoothTube";
                    roughness = 0;//mm
                }

                if (position == 1)
                {
                    tubeMaterial = "steel";
                    roughness = 0.045;//mm
                }

                if (position == 2)
                {
                    tubeMaterial = "stainlessSteel";
                    roughness = 0.002;//mm
                }

                if (position == 3)
                {
                    tubeMaterial = "glass";
                    roughness = 0;//mm
                }

                if (position == 4)
                {
                    tubeMaterial = "plastic";
                    roughness = 0;//mm
                }

                if (position == 5)
                {
                    tubeMaterial = "wood";
                    roughness = 0.5;//mm
                }

                if (position == 6)
                {
                    tubeMaterial = "rubberSmoothed";
                    roughness = 0.01;//mm
                }

                if (position == 7)
                {
                    tubeMaterial = "copper";
                    roughness = 0.0015;//mm
                }

                if (position == 8)
                {
                    tubeMaterial = "brass";
                    roughness = 0.0015;//mm
                }

                if (position == 9)
                {
                    tubeMaterial = "castIron";
                    roughness = 0.26;//mm
                }

                if (position == 10)
                {
                    tubeMaterial = "galvanizedIron";
                    roughness = 0.15;//mm
                }

                if (position == 11)
                {
                    tubeMaterial = "wrougthIron";
                    roughness = 0.046;//mm
                }

                if (position == 12)
                {
                    tubeMaterial = "concrete";
                    roughness = 1;//mm, range: 0.9 to 9 mm
                }
                roughness =roughness/1000;
                updateCalculation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
            }
        });//cierra spinnerTubeMaterial

        spinnerFluid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                if (position == 0)//Water
                {
                    selectedFluid = "Water";
                    fluidKinematicViscosity = 1.02e-6;//m2/s, water at 20°
                    fluidDensity = 997;//kg/m3 at 20°C
                }

                if (position == 1)//Oil
                {
                    selectedFluid = "Oil";
                    fluidKinematicViscosity = 9.429e-4;//m2/s, oil at 20°
                    fluidDensity = 888.1;//kg/m3 at 20°C
                }

                if (position == 2)//Alcohol Methyl
                {
                    selectedFluid = "Alcohol";
                    fluidKinematicViscosity = 7.1e-7;//m2/s, Alcohol at 20°
                    fluidDensity = 789;//kg/m3 at 20°C
                }
                updateCalculation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
            }
        });//cierra spinnerFluid

        spinnerDiam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                if (position == 0) // 1/8"
                    Diameter = 0.265;

                if (position == 1)// 1/4"
                    Diameter = 0.36;

                if (position == 2)// 3/8"
                    Diameter = 0.489;

                if (position == 3)// 1/2"
                    Diameter = 0.618;

                if (position == 4)// 3/4"
                    Diameter = 0.82;

                if (position == 5)// 1"
                    Diameter = 1.043;

                if (position == 6)// 1-1/4"
                    Diameter = 1.374;

                if (position == 7)// 1-1/2"
                    Diameter = 1.604;

                if (position == 8)// 2"
                    Diameter = 2.059;

                if (position == 9)// 2 1/2"
                    Diameter = 2.459;

                if (position == 10)// 3"
                    Diameter = 3.058;

                if (position == 11)// 3 1/2"
                    Diameter = 3.538;

                if (position == 12)// 4"
                    Diameter = 4.016;

                if (position == 13)// 5"
                    Diameter = 5.037;

                if (position == 14)// 6"
                    Diameter = 6.053;

                if (position == 15)// 8"
                    Diameter = 7.967;

                Diameter = Diameter*25.4/1000;//conversion to m
                updateCalculation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
            }
        });//cierra spinnerDiam



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.helpLicense:
                displayLicensesAlertDialog();
                return true;
           }

        return super.onOptionsItemSelected(item);
    }

    private void displayLicensesAlertDialog() {
        WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");
        AlertDialog mAlertDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(getString(R.string.action_licenses))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}

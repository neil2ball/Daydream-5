package com.example.daydream5;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;

import android.graphics.Bitmap;
import android.graphics.Point;

import android.text.TextUtils;

import android.util.Log;

import android.view.View;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView qrCodeIV;
    private RadioButton middayRadBtn;
    private RadioButton eveningRadBtn;
    private RadioButton bothRadBtn;
    private Switch ezMatchSwitch;

    Bitmap bitmap;
    QRGEncoder qrgEncoder;

    private EditText dataEdtDays;
    private EditText dataEdtPlays;

    List<List<Byte[]>> fantasy5Numbers = new ArrayList<>();
    List<Byte> numbersList = new ArrayList<>();
    List<List<Boolean>> pickTrackerList = new ArrayList<>();
    List<String> pickStrings = new ArrayList<>();
    String prefixString;
    List<String> playStrings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeIV = findViewById(R.id.idIVQrcode);

        Button generateQrBtn = findViewById(R.id.idBtnGenerateQR);

        middayRadBtn = findViewById(R.id.radioButtonMidday);

        eveningRadBtn = findViewById(R.id.radioButtonEvening);

        bothRadBtn = findViewById(R.id.radioButtonBoth);

        ezMatchSwitch = findViewById(R.id.switchEzMatch);

        dataEdtDays = findViewById(R.id.editTextNumberDays);

        dataEdtPlays = findViewById(R.id.editTextNumberPlays);

        //https://www.geeksforgeeks.org/how-to-generate-qr-code-in-android/
        // initializing onclick listener for button.
        generateQrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                numbersList = shuffleByteList();

                createPickTracker(pickTrackerList);

                fantasy5Numbers = csvReader("file.csv");

                int dayAmount = 1;
                int playAmount = 1;

                try {
                    if (TextUtils.isEmpty(dataEdtDays.getText().toString()) || TextUtils.isEmpty(dataEdtPlays.getText().toString())) {

                        if (TextUtils.isEmpty(dataEdtDays.getText().toString())) {
                            dataEdtDays.setText("1");
                        }
                        if (TextUtils.isEmpty(dataEdtPlays.getText().toString())) {
                            dataEdtPlays.setText("1");
                        }

                    }

                    dayAmount = Integer.parseInt(dataEdtDays.getText().toString());
                    if (dayAmount < 1) {
                        dayAmount = 1;
                    }
                    else if (dayAmount > 30) {
                        dayAmount = 30;
                    }

                    playAmount = Integer.parseInt(dataEdtPlays.getText().toString());

                    if (playAmount < 1) {
                        playAmount = 1;
                    }
                    else if (playAmount > fantasy5Numbers.size()) {
                        playAmount = fantasy5Numbers.size();
                    }
                }
                catch (Exception e) {
                    dayAmount = 1;
                    playAmount = 1;
                }

                StringBuilder pickPrefix = new StringBuilder();

                pickPrefix.append("lot40:WFD");

                pickPrefix.append(String.format(Locale.US, "%02d", dayAmount));

                pickPrefix.append("T");

                if (middayRadBtn.isChecked()) {
                    pickPrefix.append("M");
                }
                else if (eveningRadBtn.isChecked()) {
                    pickPrefix.append("E");
                }
                else if (bothRadBtn.isChecked()) {
                    pickPrefix.append("B");
                }

                pickPrefix.append("E");

                if (ezMatchSwitch.isChecked()) {
                    pickPrefix.append("Y");
                }
                else {
                    pickPrefix.append("N");
                }

                pickPrefix.append("S");

                prefixString = pickPrefix.toString();

                sortPicks(playAmount);

                qrStringMaker();

                qrMaker(qrCodeIV, playStrings.get(0));

            }
        });
    }

    //https://www.stackchief.com/blog/How%20to%20read%20a%20CSV%20file%20in%20Java%20%7C%20with%20examples
    protected List<List<Byte[]>> csvReader (String fileName) {
        List<List<Byte[]>> listsList = new ArrayList<>();
        List<Byte[]> lines = new ArrayList<>();

        List<Byte[]> set0 = new ArrayList<>();
        List<Byte[]> set1 = new ArrayList<>();
        List<Byte[]> set2 = new ArrayList<>();
        List<Byte[]> set3 = new ArrayList<>();
        String delimiter = ",";
        String line;
        try (BufferedReader br =
                     new BufferedReader(new FileReader(fileName))) {
            while((line = br.readLine()) != null){
                String[] lineArray = line.split(delimiter);
                Byte[] byteArray = new Byte[lineArray.length];

                for (byte x = 0; x < lineArray.length; x++) {
                    byteArray[x] = Byte.parseByte(lineArray[x]);
                }

                lines.add(byteArray);
            }

            int linesSize = lines.size();
            int quarterLinesSize = linesSize / 4;

            for (int x = 0; x < linesSize; x++) {
                if (x < quarterLinesSize) {
                    set0.add(lines.get(0));
                }
                else if (x < quarterLinesSize * 2) {
                    set1.add(lines.get(0));
                }
                else if (x < quarterLinesSize * 3) {
                    set2.add(lines.get(0));
                }
                else {
                    set3.add(lines.get(0));
                }
                lines.remove(0);
            }

            Collections.shuffle(set0);
            Collections.shuffle(set1);
            Collections.shuffle(set2);
            Collections.shuffle(set3);

            listsList.add(set0);
            listsList.add(set1);
            listsList.add(set2);
            listsList.add(set3);

            Collections.shuffle(listsList);

        } catch (Exception e){
            Log.e("csvReader", e.toString());
        }
        return listsList;
    }

    protected List<Byte> shuffleByteList () {
        List<Byte> picksListToShuffle;
        Byte[] picks = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36};
        //https://www.digitalocean.com/community/tutorials/shuffle-array-java
        picksListToShuffle = Arrays.asList(picks);
        Collections.shuffle(picksListToShuffle);
        return picksListToShuffle;
    }

    protected void createPickTracker (List<List<Boolean>> pickTrackerList) {
        Boolean[] pickFlag = {true, true, true, true, true, true,
                                true, true, true, true, true, true,
                                true, true, true, true, true, true,
                                true, true, true, true, true, true,
                                true, true, true, true, true, true,
                                true, true, true, true, true, true};

        List<Boolean> pickTracker = Arrays.asList(pickFlag);

        for(byte x = 0; x < 5; x++) {
            pickTrackerList.add(pickTracker);
        }
    }

    protected void sortPicks (int playCount) {

        while (fantasy5Numbers.size() > 0 && playCount > 0) {
            for (byte w = 0; w < fantasy5Numbers.size(); w++) { //maximum of 4 lists. We rotate through these lists to evenly distribute the picks among the colored groupings.
                if (playCount > 0) {
                    boolean breakFlag = false; //we must break out of the nested loops when we make a pick.
                    for (int x = 0; x < fantasy5Numbers.get(w).size(); x++) { //maximum 26244 picks per list

                        for (byte y = 0; y < pickTrackerList.get(0).size(); y++) { //maximum 36 numbers; tracks whether a specific number was used (in the first [of five] lists)
                            if (pickTrackerList.get(0).get(y)) { //Use the first list to find what numbers we have not used yet; the attempt is to create a lottery wheel.
                                for (byte z = 0; z < fantasy5Numbers.get(w).get(x).length; z++) { //max of 5 numbers per pick

                                    if (numbersList.get(y).equals(fantasy5Numbers.get(w).get(x)[z])) { //if the needed number is contained in the pick
                                        //We need to know the address of each pick. That way we can randomize each number in the picks and keep track of their uses.
                                        Byte[] picksIndex = new Byte[5];

                                        for (byte a = 0; a < fantasy5Numbers.get(w).get(x).length; a++) { //For each number in the pick
                                            for (byte b = 0; b < numbersList.size(); b++) { //for each address of each number in the list of possible numbers
                                                if (fantasy5Numbers.get(w).get(x)[a].equals(numbersList.get(b))) { //if the number in the pick matches the number in the possibility list
                                                    picksIndex[a] = b; //match the number in the pick with the address of the number in the possibility list for use in comparing to the pick tracker lists
                                                }
                                            }
                                        }
                                        /*We also need to know the list in which to find our picks. We need the picks addresses to help find their locations in the tracker lists.
                                        If we do not find a true value, then the pick tracker list index will remain -1 and we will not use that value.
                                        This is so that we can prevent the use of any number more than five times. We can only guarantee the use of every number without a remainder in five lists.
                                        36/5 = 7 r1. So we make a grid of five lists with 36 values. We ensure the first list is one we always attempt to fill first. When we find that number, then
                                        we search all of them for the first instance where we can use the rest of the numbers. When the first list is full, we move the other lists forward
                                        so that the second list becomes the first and we check for the uses of the remaining numbers in those lists.*/
                                        Byte[] pickTrackerListIndex = {-1, -1, -1, -1, -1};

                                        for (byte c = 0; c < pickTrackerListIndex.length; c++) { //maximum of five numbers for each of the numbers in the pick
                                            for (byte d = 0; d < pickTrackerList.size(); d++) { //maximum of five lists
                                                for (byte e = 0; e < pickTrackerList.get(d).size(); e++) { //maximum of 36 list items (booleans)

                                                    if (picksIndex[c] == e && pickTrackerList.get(d).get(e)) { //If the picksIndex address is equal to pick tracker list address and the pick tracker list item is true
                                                        pickTrackerListIndex[c] = d; //then assign the pick tracker list index address to the array item corresponding to the number in the pick
                                                        break; //break out of this loop so that we ensure that we use the first instance of true
                                                    }
                                                }

                                                if (pickTrackerListIndex[c] == d) {
                                                    break;//break out of this loop too so that we ensure that we use the first instance of true
                                                }
                                            }
                                        }

                                        //If none of the numbers are used five times already
                                        if (pickTrackerListIndex[0] != -1 &&
                                                pickTrackerListIndex[1] != -1 &&
                                                pickTrackerListIndex[2] != -1 &&
                                                pickTrackerListIndex[3] != -1 &&
                                                pickTrackerListIndex[4] != -1) {

                                            try {
                                                StringBuilder pickString = new StringBuilder(); //We are going to save the strings in a list for use in creating the qr codes.
                                                //https://stackoverflow.com/questions/48872155/string-concatenation-in-loop
                                                String pickNumberString;
                                                for (byte f = 0; f < fantasy5Numbers.get(w).get(x).length; f++) {
                                                    pickNumberString = String.format(Locale.US, "%02d", fantasy5Numbers.get(w).get(x)[f]); //We need to make the bytes two digits no matter what.
                                                    //We start with bytes to attempt to save memory.

                                                    pickString.append(pickNumberString); //append the values as a string

                                                }

                                                pickStrings.add(pickString.toString()); //add the string of the numbers in the pick to the list of picks
                                                fantasy5Numbers.get(w).remove(x); //remove the pick from the list of picks. This is how we keep track of when to stop using the lists.
                                                playCount--;
                                                if (fantasy5Numbers.get(w).size() == 0) {
                                                    fantasy5Numbers.remove(w); //remove one of the four list groups if there are no more numbers in that list.
                                                }

                                                for (byte h = 0; h < picksIndex.length; h++) {
                                                    pickTrackerList.get(pickTrackerListIndex[h]).set(picksIndex[h], false); //set the specific numbers of the pick to false.
                                                }

                                                breakFlag = true;

                                                break; //break out of this loop so that we can break out of the outer loops.
                                            } catch (Exception e) {
                                                Log.e("sortPicks", e.toString());
                                            }
                                        }
                                    }
                                }

                                if (breakFlag) {
                                    break; //break out of this loop so that we can break out of the outer loop.
                                }
                            } else if (y == pickTrackerList.get(0).size() - 1) { //This is used when there are no remaining picks in the first pick tracker list.
                                List<List<Boolean>> pickTrackerBuffer = new ArrayList<>();

                                createPickTracker(pickTrackerBuffer); //This creates a fresh tracker list.

                                for (byte i = 0; i < pickTrackerList.size() - 1; i++) {
                                    pickTrackerBuffer.set(i, pickTrackerList.get(i + 1)); //set the values of pickTrackerBuffer to the values of PickTrackerList of the remaining lists.
                                }

                                pickTrackerList = pickTrackerBuffer; //replace pickTrackerList with pickTrackerBuffer. This places lists 1,2,3,4 in lists 0,1,2,3 leaving all of the new 4 true.
                            }
                        }

                        if (breakFlag) {
                            break; //break out of this loop so that we can move on to the next list.
                        }
                    }
                }
                else {
                    break;
                }
            }
        }
        fantasy5Numbers.clear();
    }

    protected void qrMaker (ImageView qrCodeIV, String dataEdt) {
        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = Math.min(width, height);
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(dataEdt, null, QRGContents.Type.TEXT, dimen);
        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder.getBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            qrCodeIV.setImageBitmap(bitmap);
        }
        catch (Exception e) {
            // this method is called for
            // exception handling.
            Log.e("qrMaker", e.toString());
        }
    }

    protected void qrStringMaker () {

        int pickStringsPlayslipCount = pickStrings.size() / 10;
        for (int x = 0; x < pickStringsPlayslipCount; x++) {
            StringBuilder playStringBuilder = new StringBuilder();
            playStringBuilder.append(prefixString);
            for (int y = 0; y < 10; y++) {
                playStringBuilder.append(pickStrings.get(y));
                pickStrings.remove(y);
            }
            playStrings.add(playStringBuilder.toString());
        }
        if (pickStrings.size() % 10 != 0) {
            StringBuilder playStringBuilder = new StringBuilder();
            playStringBuilder.append(prefixString);
            for (int y = 0; y < pickStrings.size(); y++) {
                playStringBuilder.append(pickStrings.get(y));
                pickStrings.remove(y);
            }
            playStrings.add(playStringBuilder.toString());
        }
    }
}
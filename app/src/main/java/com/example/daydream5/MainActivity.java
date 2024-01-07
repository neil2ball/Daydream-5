package com.example.daydream5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.media.MediaScannerConnection;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;

import android.graphics.Bitmap;
import android.graphics.Color;

import android.text.TextUtils;

import android.util.Log;

import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
public class MainActivity extends AppCompatActivity {

    private ImageView qrCodeIV;
    private RadioButton middayRadBtn;
    private RadioButton eveningRadBtn;
    private RadioButton bothRadBtn;
    private Switch ezMatchSwitch;

    Bitmap bitmap;

    private EditText dataEdtDays;
    private EditText dataEdtPlays;

    List<List<Byte[]>> fantasy5Numbers = new ArrayList<>();
    List<Byte[]> pickBytes = new ArrayList<>();

    List<List<Byte[]>> paperPlayslipLists = new ArrayList<>();
    List<Byte> numbersList = new ArrayList<>();
    List<List<Boolean>> pickTrackerList = new ArrayList<>();
    List<String> pickStrings = new ArrayList<>();
    String prefixString;
    List<String> playStrings = new ArrayList<>();

    int dayAmount = 1;
    int playAmount = 1;
    boolean boolFlag = false;
    int pickCount = 0;
    int picksSize = 0;

    private boolean qrCodeFlag = false;

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
                    else if (playAmount > fantasy5Numbers.get(0).size() * 4) {
                        playAmount = fantasy5Numbers.get(0).size() * 4;
                    }

                }
                catch (Exception e) {
                    dayAmount = 1;
                    playAmount = 1;
                }

                if (qrCodeFlag) {
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

                    try {
                        qrMaker(qrCodeIV, playStrings.get(0));
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else {

                    if (dayAmount > 30) {
                        dayAmount = 30;
                    }
                    else if (dayAmount > 28) {
                        dayAmount = 28;
                    }
                    else if (dayAmount > 21) {
                        dayAmount = 21;
                    }
                    else if (dayAmount > 14) {
                        dayAmount = 14;
                    }
                    else if (dayAmount > 10) {
                        dayAmount = 10;
                    }
                    else if (dayAmount > 7) {
                        dayAmount = 7;
                    }
                    else if (dayAmount > 5) {
                        dayAmount = 5;
                    }

                    Toast.makeText(MainActivity.this, "Sort Picks Begin",
                            Toast.LENGTH_SHORT).show();
                    sortPicks(playAmount);
                    Toast.makeText(MainActivity.this, String.valueOf(pickBytes.size()),
                            Toast.LENGTH_LONG).show();

                    paperPlayslipListsMaker();

                    long dateFolderName = new Date().getTime();
                    int paperPlayslipListsSize =paperPlayslipLists.size();
                    for (int x = 0; x < paperPlayslipListsSize; x++) {

                        BitMatrix bitmapMatrix = new BitMatrix(816, 312);
                        bitmapMatrix.clear();

                        Bitmap queueBitmap = paperPlayslipMaker(bitmapMatrix);

                        saveImage(queueBitmap, String.valueOf(dateFolderName));

                        if (x == paperPlayslipListsSize - 1) {
                            qrCodeIV.setImageBitmap(Bitmap.createScaledBitmap(queueBitmap, 204, 78, false));
                        }
                    }



                }
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
                     new BufferedReader(new InputStreamReader(getAssets().open(fileName)))) {
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

        for(byte x = 0; x < 6; x++) {
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

                                        //If none of the numbers are used six times already
                                        if (pickTrackerListIndex[0] != -1 &&
                                                pickTrackerListIndex[1] != -1 &&
                                                pickTrackerListIndex[2] != -1 &&
                                                pickTrackerListIndex[3] != -1 &&
                                                pickTrackerListIndex[4] != -1) {

                                            try {

                                                if (qrCodeFlag) {
                                                    StringBuilder pickString = new StringBuilder(); //We are going to save the strings in a list for use in creating the qr codes.
                                                    //https://stackoverflow.com/questions/48872155/string-concatenation-in-loop
                                                    String pickNumberString;
                                                    for (byte f = 0; f < fantasy5Numbers.get(w).get(x).length; f++) {
                                                        pickNumberString = String.format(Locale.US, "%02d", fantasy5Numbers.get(w).get(x)[f]); //We need to make the bytes two digits no matter what.
                                                        //We start with bytes to attempt to save memory.

                                                        pickString.append(pickNumberString); //append the values as a string

                                                    }

                                                    pickStrings.add(pickString.toString()); //add the string of the numbers in the pick to the list of picks
                                                }
                                                else {
                                                    pickBytes.add(fantasy5Numbers.get(w).get(x));
                                                }

                                                fantasy5Numbers.get(w).remove(x); //remove the pick from the list of picks. This is how we keep track of when to stop using the lists.
                                                playCount--;

                                                if (fantasy5Numbers.get(w).size() == 0) {
                                                    fantasy5Numbers.remove(w); //remove one of the four list groups if there are no more numbers in that list.
                                                }

                                                for (byte h = 0; h < picksIndex.length; h++) {
                                                    pickTrackerList.get(pickTrackerListIndex[h]).set(picksIndex[h], false); //set the specific numbers of the pick to false.
                                                }

                                                breakFlag = true;

                                                break;

                                            } catch (Exception e) {
                                                Log.e("sortPicks", e.toString());
                                            }
                                        }
                                    }
                                }
                                if (breakFlag) {
                                    break;
                                }
                            }
                            if (y == pickTrackerList.get(0).size() - 1) { //This is used when there are possibly no remaining picks in the first pick tracker list.

                                if (pickCount == 0) {
                                    for (byte z = 0; z < pickTrackerList.get(0).size(); z++) {
                                        if (pickTrackerList.get(0).get(z)) {
                                            boolFlag = true;
                                            break;
                                        }
                                    }
                                }

                                if (picksSize == 0) {
                                    for (byte a = 0; a < fantasy5Numbers.size(); a++) {
                                        picksSize += fantasy5Numbers.get(a).size();
                                    }
                                }

                                /*if (pickCount % 4000 == 0) {
                                    Toast.makeText(MainActivity.this, y + " boolTracker " + playCount + " " + boolFlag + " " + pickCount + " " + numbersList.get(y),
                                            Toast.LENGTH_SHORT).show();
                                }*/

                                if (boolFlag && pickCount < picksSize) {
                                    pickCount++;
                                }
                                else {
                                    List<List<Boolean>> pickTrackerBuffer = new ArrayList<>();

                                    createPickTracker(pickTrackerBuffer); //This creates a fresh tracker list.

                                    for (byte i = 0; i < pickTrackerList.size() - 1; i++) {
                                        pickTrackerBuffer.set(i, pickTrackerList.get(i + 1)); //set the values of pickTrackerBuffer to the values of PickTrackerList of the remaining lists.
                                    }

                                    pickTrackerList = pickTrackerBuffer; //replace pickTrackerList with pickTrackerBuffer. This places lists 1,2,3,4 in lists 0,1,2,3 leaving all of the new 4 true.
                                    boolFlag = false;
                                    pickCount = 0;
                                    picksSize = 0;
                                }

                            }
                        }
                        if (breakFlag) {
                            break;
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

    protected void qrMaker (ImageView qrCodeIV, String dataEdt) throws UnsupportedEncodingException, WriterException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-2022-JP");
        //hints.put(EncodeHintType.QR_MASK_PATTERN, 7);


        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(dataEdt.getBytes(), "ISO-2022-JP"),
                BarcodeFormat.QR_CODE, 25, 25, hints);

        try {
            //https://github.com/nayuki/QR-Code-generator/blob/master/java/QrCodeGeneratorDemo.java
            //https://stackoverflow.com/questions/8800919/how-to-generate-a-qr-code-for-an-android-application/25283174#25283174

            bitmap = Bitmap.createBitmap(25, 25, Bitmap.Config.RGB_565);
            for (int x = 0; x < 25; x++) {
                for (int y = 0; y < 25; y++) {

                    bitmap.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);

                }
            }
            //qrCodeIV.setImageBitmap(bitmap);
            qrCodeIV.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 256, 256, false));
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
            }

            for (int y = 0; y < 10; y++) {
                pickStrings.remove(y);
            }
            playStrings.add(playStringBuilder.toString());
        }
        if (pickStrings.size() % 10 != 0) {
            StringBuilder playStringBuilder = new StringBuilder();
            playStringBuilder.append(prefixString);

            for (int y = 0; y < pickStrings.size(); y++) {
                playStringBuilder.append(pickStrings.get(y));
            }

            for (int y = 0; y < pickStrings.size(); y++) {
                pickStrings.remove(y);
            }
            playStrings.add(playStringBuilder.toString());
        }
    }

    protected Bitmap paperPlayslipMaker (BitMatrix bitmapMatrix) {
        if (dayAmount == 30) {
            bitmapMatrix = blackSquare(bitmapMatrix, 495, 9);
        }
        else if (dayAmount == 28) {
            bitmapMatrix = blackSquare(bitmapMatrix, 478, 9);
        }
        else if (dayAmount == 21) {
            bitmapMatrix = blackSquare(bitmapMatrix, 461, 9);
        }
        else if (dayAmount == 14) {
            bitmapMatrix = blackSquare(bitmapMatrix, 444, 9);
        }
        else if (dayAmount == 10) {
            bitmapMatrix = blackSquare(bitmapMatrix, 427, 9);
        }
        else if (dayAmount == 7) {
            bitmapMatrix = blackSquare(bitmapMatrix, 410, 9);
        }
        else if (dayAmount == 5) {
            bitmapMatrix = blackSquare(bitmapMatrix, 393, 9);
        }
        else if (dayAmount == 4) {
            bitmapMatrix = blackSquare(bitmapMatrix, 375, 9);
        }
        else if (dayAmount == 3) {
            bitmapMatrix = blackSquare(bitmapMatrix, 358, 9);
        }
        else if (dayAmount == 2) {
            bitmapMatrix = blackSquare(bitmapMatrix, 341, 9);
        }

        if (middayRadBtn.isChecked()) {
            bitmapMatrix = blackSquare(bitmapMatrix, 100, 44);
        }
        else if (eveningRadBtn.isChecked()) {
            bitmapMatrix = blackSquare(bitmapMatrix, 100, 61);
        }
        else if (bothRadBtn.isChecked()) {
            bitmapMatrix = blackSquare(bitmapMatrix, 100, 78);
        }

        bitmapMatrix = paperPlayslipFill(bitmapMatrix);

        Bitmap bitmap = Bitmap.createBitmap(816, 312, Bitmap.Config.RGB_565);

        for (int x = 0; x < 816; x++) {
            for (int y = 0; y < 312; y++) {

                bitmap.setPixel(x, y, bitmapMatrix.get(x,y) ? Color.BLACK : Color.WHITE);

            }
        }
        return bitmap;
    }

    protected BitMatrix blackSquare(BitMatrix bitmap, int startX, int startY) {

        bitmap.setRegion(startX, startY, 13, 13);

        return bitmap;
    }

    protected void paperPlayslipListsMaker () {

        int pickBytesPlayslipCount = pickBytes.size() / 10;

        for (int x = 0; x < pickBytesPlayslipCount; x++) {
            List<Byte[]> paperPlayslipBytes = new ArrayList<>();

            for (int y = 0; y < 10; y++) {
                paperPlayslipBytes.add(pickBytes.get(y));
            }

            for (int z = 0; z < 10; z++) {
                pickBytes.remove(0);
            }

            paperPlayslipLists.add(paperPlayslipBytes);
        }
        if (pickBytes.size() % 10 != 0) {
            List<Byte[]> paperPlayslipBytes = new ArrayList<>();

            for (int y = 0; y < pickBytes.size(); y++) {
                paperPlayslipBytes.add(pickBytes.get(y));
            }

            for (int z = 0; z < pickBytes.size(); z++) {
                pickBytes.remove(0);
            }

            paperPlayslipLists.add(paperPlayslipBytes);
        }

    }

    protected BitMatrix paperPlayslipFill (BitMatrix bitmapMatrix) {

        int xIndex = 0;
        int yIndex = 0;

        if (paperPlayslipLists.size() > 0) {
            for (byte x = 0; x < paperPlayslipLists.get(0).size(); x++) {

                switch (x) {
                    case 0:
                        xIndex = 117;
                        yIndex = 43;
                        break;
                    case 1:
                        xIndex = 238;
                        yIndex = 44;
                        break;
                    case 2:
                        xIndex = 358;
                        yIndex = 43;
                        break;
                    case 3:
                        xIndex = 478;
                        yIndex = 43;
                        break;
                    case 4:
                        xIndex = 598;
                        yIndex = 43;
                        break;
                    case 5:
                        xIndex = 117;
                        yIndex = 181;
                        break;
                    case 6:
                        xIndex = 238;
                        yIndex = 181;
                        break;
                    case 7:
                        xIndex = 358;
                        yIndex = 181;
                        break;
                    case 8:
                        xIndex = 478;
                        yIndex = 181;
                        break;
                    case 9:
                        xIndex = 598;
                        yIndex = 181;
                        break;

                }

                for (byte y = 0; y < paperPlayslipLists.get(0).get(x).length; y++) {

                    int yPanelIndex = yIndex;
                    switch (paperPlayslipLists.get(0).get(x)[y]) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            break;
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                            yPanelIndex += 17;
                            break;
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                            yPanelIndex += 17 * 2;
                            break;
                        case 19:
                        case 20:
                        case 21:
                        case 22:
                        case 23:
                        case 24:
                            yPanelIndex += 17 * 3;
                            break;
                        case 25:
                        case 26:
                        case 27:
                        case 28:
                        case 29:
                        case 30:
                            yPanelIndex += 17 * 4;
                            break;
                        case 31:
                        case 32:
                        case 33:
                        case 34:
                        case 35:
                        case 36:
                            yPanelIndex += 17 * 5;
                            break;
                    }

                    int xPanelIndex = xIndex;
                    switch (paperPlayslipLists.get(0).get(x)[y]) {
                        case 1:
                        case 7:
                        case 13:
                        case 19:
                        case 25:
                        case 31:
                            break;
                        case 2:
                        case 8:
                        case 14:
                        case 20:
                        case 26:
                        case 32:
                            xPanelIndex += 17;
                            break;
                        case 3:
                        case 9:
                        case 15:
                        case 21:
                        case 27:
                        case 33:
                            xPanelIndex += 17 * 2;
                            break;
                        case 4:
                        case 10:
                        case 16:
                        case 22:
                        case 28:
                        case 34:
                            xPanelIndex += 17 * 3;
                            break;
                        case 5:
                        case 11:
                        case 17:
                        case 23:
                        case 29:
                        case 35:
                            xPanelIndex += 17 * 4;
                            break;
                        case 6:
                        case 12:
                        case 18:
                        case 24:
                        case 30:
                        case 36:
                            xPanelIndex += 17 * 5;
                            break;
                    }
                    bitmapMatrix = blackSquare(bitmapMatrix, xPanelIndex, yPanelIndex);
                }

                if (ezMatchSwitch.isChecked()) {
                    switch (x) {
                        case 0:
                            xIndex = 220;
                            yIndex = 130;
                            break;
                        case 1:
                            xIndex = 341;
                            yIndex = 130;
                            break;
                        case 2:
                            xIndex = 461;
                            yIndex = 130;
                            break;
                        case 3:
                            xIndex = 580;
                            yIndex = 130;
                            break;
                        case 4:
                            xIndex = 700;
                            yIndex = 130;
                            break;
                        case 5:
                            xIndex = 220;
                            yIndex = 268;
                            break;
                        case 6:
                            xIndex = 340;
                            yIndex = 268;
                            break;
                        case 7:
                            xIndex = 461;
                            yIndex = 268;
                            break;
                        case 8:
                            xIndex = 580;
                            yIndex = 268;
                            break;
                        case 9:
                            xIndex = 701;
                            yIndex = 268;
                            break;

                    }

                    bitmapMatrix = blackSquare(bitmapMatrix, xIndex, yIndex);
                }
            }

            paperPlayslipLists.remove(0);
        }

        return bitmapMatrix;
    }

    //https://stackoverflow.com/questions/77072796/new-android-storage-permission-for-api-13-and-above
    private void saveImage(Bitmap bitmap, String folderName) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Daydream5_" + folderName);

        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // Generate a unique file name
        String imageName = "Playslip_" + new Date().getTime() + ".jpg";

        File file = new File(myDir, imageName);
        if (file.exists()) file.delete();

        try {
            // Save the Bitmap to the file
            OutputStream outputStream;
            outputStream = Files.newOutputStream(file.toPath());

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Add the image to the MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // Trigger a media scan to update the gallery
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, null, null);
        } catch (Exception e) {
            // TODO
        }
    }
}
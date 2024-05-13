package com.example.check;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<AddressInfo> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        listView = view.findViewById(R.id.listView);
        adapter = new ArrayAdapter<AddressInfo>(requireContext(), R.layout.list_item_layout) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
                }

                // Get the current item
                AddressInfo currentItem = getItem(position);

                // Set address and info to TextViews
                TextView textAddress = convertView.findViewById(R.id.textAddress);
                TextView textInfo = convertView.findViewById(R.id.textInfo);
                if (currentItem != null) {
                    textAddress.setText(currentItem.getAddress());
                    String[] infoNumbers = currentItem.getInfoNumbers();
                    if (infoNumbers != null && infoNumbers.length > 0) {
                        textInfo.setMovementMethod(LinkMovementMethod.getInstance());
                        SpannableStringBuilder spannable = new SpannableStringBuilder();
                        int count = 0;
                        for (String phoneNumber : infoNumbers) {
                            if (!phoneNumber.isEmpty()) {
                                ClickableSpan clickableSpan = new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View view) {
                                        // Handle click event, e.g., initiate a phone call
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:" + "+" + phoneNumber));
                                        startActivity(intent);
                                    }
                                };
                                spannable.append("+").append(phoneNumber).append("\n");
                                spannable.setSpan(clickableSpan, spannable.length() - phoneNumber.length() - 1, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                ForegroundColorSpan greySpan = new ForegroundColorSpan(Color.parseColor("#CACACA"));
                                spannable.setSpan(greySpan, spannable.length() - phoneNumber.length() - 1, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                count++;
                                if (count == 5) {
                                    break;
                                }
                            }
                        }
                        textInfo.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                }


                return convertView;
            }
        };
        listView.setAdapter(adapter);

        new ParseDataTask().execute();

        return view;
    }

    private class ParseDataTask extends AsyncTask<Void, Void, List<AddressInfo>> {

        @Override
        protected List<AddressInfo> doInBackground(Void... voids) {
            List<AddressInfo> dataList = new ArrayList<>();
            try {
                Document document = Jsoup.connect("https://asbleasing.by/contacts-list/").get();

                Elements contactsItems = document.select(".contacts-item");
                for (Element item : contactsItems) {
                    String address = item.select(".contacts-item__address").text();

                    String infoText = item.select(".contacts-item-info__link").text();
                    String[] infoNumbers = infoText.split("\\+");
                    StringBuilder infoBuilder = new StringBuilder();
                    for (int i = 0; i < infoNumbers.length; i++) {
                        String phoneNumber = infoNumbers[i].trim();
                        if (!phoneNumber.isEmpty()) {
                            infoBuilder.append(phoneNumber).append("\n");
                        }
                    }
                    dataList.add(new AddressInfo(address, infoBuilder.toString(), infoNumbers));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dataList;
        }

        @Override
        protected void onPostExecute(List<AddressInfo> dataList) {
            super.onPostExecute(dataList);

            // Clear the adapter
            adapter.clear();

            // Add data to the adapter
            adapter.addAll(dataList);
        }
    }

    // Data class to hold address and info
    private static class AddressInfo {
        private String address;
        private String info;
        private String[] infoNumbers;

        AddressInfo(String address, String info, String[] infoNumbers) {
            this.address = address;
            this.info = info;
            this.infoNumbers = infoNumbers;
        }

        String getAddress() {
            return address;
        }

        String getInfo() {
            return info;
        }

        String[] getInfoNumbers() {
            return infoNumbers;
        }
    }
}

package com.roaa.shops_customers.BottomNavigation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.roaa.shops_customers.AboutActivity;
import com.roaa.shops_customers.BuildConfig;
import com.roaa.shops_customers.LibraryTabs.BookmarkFragment;
import com.roaa.shops_customers.ModelClasses.UserProfileClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.roaa.shops_customers.R;
import com.roaa.shops_customers.UserProfileEditActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;


public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";

    //layout widgets
    private TextView name, number, email;
    private CircleImageView userImage;
    private MaterialButton editProfileButton, bookmarkButton;
    private LinearLayout mainContentLayout;
    private ProgressBar imageProgressBar;
    private LinearLayout addSeller;
    private RelativeLayout shareButton, librariesButton, feedbackButton, helpButton, rateUsButton;

    //vars
    private SharedPreferences preferences;
    private UserProfileClass userProfileClass;
    private DatabaseClass databaseClass;
    private String userImageString = "";
    private View view;


    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account, container, false);
        initFields();

        setFields();

        editProfileButton.setOnClickListener(v -> sendUserToEditProfileActivity());

        shareButton.setOnClickListener(v -> shareApp());

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_activity_frame_layout_fragment_container, new BookmarkFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });
        feedbackButton.setOnClickListener(v -> sendEmail());

        librariesButton.setOnClickListener(v -> sendUserTLibrariesActivity());

        rateUsButton.setOnClickListener(v -> rateUsMethod());

        addSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TreeShop Seller");
                String shareMessage = "\nBuy things from your area at lowest price and save money\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + "com.roaa.shops_seller" + "\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            }
        });

        helpButton.setOnClickListener(v -> sendEmail());

        return view;
    }


    private void sendEmail() {

        Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "shoptree2021.help@gmail.com")); // mention an email id here
        sendEmail.putExtra(Intent.EXTRA_SUBJECT, "subject"); //subject of the email
        sendEmail.putExtra(Intent.EXTRA_TEXT, "body"); //body of the email
        startActivity(Intent.createChooser(sendEmail, "Choose one"));
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TreeShop");
            String shareMessage = "\nBuy things from your area at lowest price and save money\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        databaseClass.gettingUserProfileDataFromDatabaseWithCallback(new FirestoreCallback() {
            @Override
            public void dataGetComplete() {
                setFields();
            }
        });

    }


    private void initFields() {

        name = view.findViewById(R.id.user_profile_activity_text_view_name);
        number = view.findViewById(R.id.user_profile_activity_text_view_number);
        email = view.findViewById(R.id.user_profile_activity_text_view_email);
        userImage = view.findViewById(R.id.user_profile_activity_circle_image_view_user_image);
        editProfileButton = view.findViewById(R.id.user_profile_activity_button_edit_profile);
        preferences = getActivity().getSharedPreferences("userProfile", MODE_PRIVATE);
        databaseClass = new DatabaseClass(getContext());
        mainContentLayout = view.findViewById(R.id.user_profile_activity_linear_layout_main_content);
        imageProgressBar = view.findViewById(R.id.account_fragemnt_image_progress_bar);
        shareButton = view.findViewById(R.id.account_fragment_relative_layout_share_button);
        librariesButton = view.findViewById(R.id.account_fragment_relative_layout_libraries_button);
        bookmarkButton = view.findViewById(R.id.user_profile_activity_button_bookmark_button);
        feedbackButton = view.findViewById(R.id.account_fragment_relative_layout_feedback_button);
        rateUsButton = view.findViewById(R.id.account_fragment_relative_layout_rate_us_button);
        helpButton = view.findViewById(R.id.account_fragment_relative_layout_help_button);
        addSeller = view.findViewById(R.id.add_seller);
    }


    private void setFields() {
        userProfileClass = databaseClass.gettingUserProfileDataFromDevice();

        try {
            name.setText(userProfileClass.getFirstName() + " " + userProfileClass.getLastName());
            number.setText(userProfileClass.getPhoneNumber());
            try {
                if (userProfileClass.getEmail() == null) {
                    email.setVisibility(View.GONE);
                } else {
                    email.setVisibility(View.VISIBLE);
                    email.setText(userProfileClass.getEmail());
                }
            } catch (Exception e) {
                email.setVisibility(View.GONE);
            }

            if (userImageString.equals(userProfileClass.getProfileImage())) {
                //do nothing
            } else {
                try {
                    imageProgressBar.setVisibility(View.VISIBLE);
                    if (userProfileClass.getProfileImage() == null) {
                        imageProgressBar.setVisibility(View.GONE);
                    } else {
                        Picasso.get().load(userProfileClass.getProfileImage()).into(userImage,
                                new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        imageProgressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        imageProgressBar.setVisibility(View.GONE);
                                    }
                                });
                        userImageString = userProfileClass.getProfileImage();
                    }


                } catch (Exception e) {
                    imageProgressBar.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserToEditProfileActivity() {
        Intent editProfileIntent = new Intent(getContext(), UserProfileEditActivity.class);
        startActivity(editProfileIntent);
    }

    private void sendUserTLibrariesActivity() {
        Intent libraryIntent = new Intent(getContext(), AboutActivity.class);
        startActivity(libraryIntent);
    }

    private void rateUsMethod() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName())));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
        }
    }
}
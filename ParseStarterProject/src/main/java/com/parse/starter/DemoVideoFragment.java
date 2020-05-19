package com.parse.starter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class DemoVideoFragment extends Fragment {
    public DemoVideoFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_demo_video, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        VideoView videoView = getActivity().findViewById(R.id.demoVideoView);
        videoView.setVideoPath("android.resource://" + getActivity().getPackageName() + "/" + R.raw.demovideo);

        MediaController mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView); //links video to media controller
        videoView.setMediaController(mediaController); //allowing that to control our videoview
        videoView.start();
    }
    public void onBackPressed() {
         getActivity().getSupportFragmentManager().popBackStack();
    }
}

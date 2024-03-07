package com.pixel.chatapp.activities;

import static com.pixel.chatapp.interface_listeners.DataModelType.Offer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.ErrorCallBack;
import com.pixel.chatapp.interface_listeners.NewEventCallBack;
import com.pixel.chatapp.interface_listeners.SuccessCallBack;
import com.pixel.chatapp.model.DataModel;
import com.pixel.chatapp.webrtc.MyPeerConnectionObserver;
import com.pixel.chatapp.webrtc.WebRTCClient;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.util.Objects;

public class MainRepository implements WebRTCClient.Listener {

    public Listener listener;
    private final Gson gson = new Gson();
//    private final FirebaseClient firebaseClient;

    private WebRTCClient webRTCClient;

    private String currentUsername;

    private SurfaceViewRenderer remoteView;

    private String target;
    private void updateCurrentUsername(String username){
        this.currentUsername = username;
    }

//    private MainRepository(){
//        this.firebaseClient = new FirebaseClient();
//    }

    MainActivity mainActivity = new MainActivity();

    private static MainRepository instance;
    public static MainRepository getInstance(){
        if (instance == null){
            instance = new MainRepository();
        }
        return instance;
    }

//    public void login(String username, Context context, SuccessCallBack callBack){
//        firebaseClient.login(username,()->{
//            updateCurrentUsername(username);
//            this.webRTCClient = new WebRTCClient(context,new MyPeerConnectionObserver(){
//                @Override
//                public void onAddStream(MediaStream mediaStream) {
//                    super.onAddStream(mediaStream);
//                    try{
//                        mediaStream.videoTracks.get(0).addSink(remoteView);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
//                    Log.d("TAG", "onConnectionChange: "+newState);
//                    super.onConnectionChange(newState);
//                    if (newState == PeerConnection.PeerConnectionState.CONNECTED && listener!=null){
//                        listener.webrtcConnected();
//                    }
//
//                    if (newState == PeerConnection.PeerConnectionState.CLOSED ||
//                            newState == PeerConnection.PeerConnectionState.DISCONNECTED ){
//                        if (listener!=null){
//                            listener.webrtcClosed();
//                        }
//                    }
//                }
//
//                @Override
//                public void onIceCandidate(IceCandidate iceCandidate) {
//                    super.onIceCandidate(iceCandidate);
//                    webRTCClient.sendIceCandidate(iceCandidate,target);
//                }
//            },username);
//            webRTCClient.listener = this;
//            callBack.onSuccess();
//        });
//    }

    //  =========== methods

    public void initialiseWebRTC(String username, Context context, String targetUid,
                                 String otherName, String senderUid)
    {
//        Log.d("TAG", "onConnectionChange: two");
        this.webRTCClient = new WebRTCClient(context,new MyPeerConnectionObserver(){

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                try{
                    mediaStream.videoTracks.get(0).addSink(remoteView);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                super.onConnectionChange(newState);
                if (newState == PeerConnection.PeerConnectionState.CONNECTED && listener!=null){
                    listener.webrtcConnected();
                }

                if (newState == PeerConnection.PeerConnectionState.CONNECTING && listener!=null){
                    listener.isConnecting();
                }

                if (newState == PeerConnection.PeerConnectionState.CLOSED ||
                        newState == PeerConnection.PeerConnectionState.DISCONNECTED ){
                    if (listener!=null){
                        listener.webrtcClosed();
                    }
                }
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
//                System.out.println("what is ice is called");
                webRTCClient.sendIceCandidate(iceCandidate, targetUid);
            }
        }, targetUid, otherName, senderUid, username);

//        WebRTCClient.listener = this;

    }


    public void initLocalView(SurfaceViewRenderer view){
        webRTCClient.initLocalSurfaceView(view);
    }

    public void initRemoteView(SurfaceViewRenderer view){
        webRTCClient.initRemoteSurfaceView(view);
        this.remoteView = view;
    }

    public void startCall(String targetUid){
        webRTCClient.call(targetUid);
    }

    public void switchCamera() {
        webRTCClient.switchCamera();
    }

    public void toggleAudio(Boolean shouldBeMuted){
        webRTCClient.toggleAudio(shouldBeMuted);
    }

    // mute and unmute video
    public void toggleVideo(Boolean shouldBeMuted){
        webRTCClient.toggleVideo(shouldBeMuted);
    }

//    public void sendCallRequest(String target, ErrorCallBack errorCallBack){
//        firebaseClient.sendMessageToOtherUser(
//                new DataModel(target,currentUsername,null, DataModelType.StartCall),errorCallBack
//        );
//    }

    public void endCall(){
        webRTCClient.closeConnection();
    }

    public void subscribeForLatestEvent(String targetUid, NewEventCallBack callBack){
        mainActivity.observeIncomingLatestEvent(targetUid, model -> {
            switch (model.getType()){

                case Offer:
//                    this.target = model.getSenderUid();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.OFFER,model.getData()
                    ));
                    webRTCClient.answer(model.getSenderUid());

                    break;
                case Answer:
//                    this.target = model.getSenderUid();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.ANSWER,model.getData()
                    ));

                    break;
                case IceCandidate:
                        try{
                            IceCandidate candidate = gson.fromJson(model.getData(),IceCandidate.class);
//                            System.out.println("what is ice here - " + candidate);
                            webRTCClient.addIceCandidate(candidate);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    break;
                case StartCall:
                case None:
//                    this.target = model.getSenderUid();
                    callBack.onNewEventReceived(model);
                    break;
            }

        });
    }

    @Override
    public void onTransferDataToOtherPeer(DataModel model) {
//        firebaseClient.sendMessageToOtherUser(model,()->{});
    }

//    @Override
//    public void onTransferDataToOtherPeer(DataModel model) {
//
//    }

    public interface Listener{
        void webrtcConnected();
        void webrtcClosed();
        void isConnecting();

    }
}

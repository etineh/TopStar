package com.pixel.chatapp.webrtc;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.pixel.chatapp.interface_listeners.DataModelType;
import com.pixel.chatapp.model.CallModel;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTCClient {

    private final Gson gson = new Gson();
    private final Context context;
    private final String username;
    private EglBase.Context eglBaseContext= EglBase.create().getEglBaseContext();
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private List<PeerConnection.IceServer> iceServer = new ArrayList<>();
    private CameraVideoCapturer videoCapturer;
    private VideoSource localVideoSource;
    private AudioSource localAudioSource;
    private String localTrackId = "local_track";
    private String localStreamId = "local_stream";
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private MediaStream localStream;
    private MediaConstraints mediaConstraints = new MediaConstraints();

    public static Listener listener;

    DatabaseReference refCall;

    String otherUid, otherName, myId;
//    CallModel dataModel = new CallModel(otherUid, otherName, user.getUid(), myUserName, null, DataModelType.Offer);

    public WebRTCClient(Context context, PeerConnection.Observer observer, String otherUid, String otherName,
                        String myId, String username) {
        this.context = context;
        this.username = username;
        this.otherUid = otherUid;
        this.otherName = otherName;
        this.myId = myId;

        refCall = FirebaseDatabase.getInstance().getReference("Calls");
        
        initPeerConnectionFactory();
        peerConnectionFactory = createPeerConnectionFactory();
        iceServer.add(PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
                .setUsername("83eebabf8b4cce9d5dbcb649")
                .setPassword("2D7JvfkOQtBdYW3R").createIceServer());
        peerConnection = createPeerConnection(observer);
        localVideoSource = peerConnectionFactory.createVideoSource(false);
        localAudioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo","true"));
    }

    //initializing peer connection section
    private void initPeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory.InitializationOptions.builder(context).
                setFieldTrials("WebRTC-H264HighProfile/Enabled/").setEnableInternalTracer(true).createInitializationOptions();
        PeerConnectionFactory.initialize(options);
    }

    private PeerConnectionFactory createPeerConnectionFactory() {
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = false;
        options.disableNetworkMonitor = false;
        return PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBaseContext,true,true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBaseContext))
                .setOptions(options).createPeerConnectionFactory();
    }

    private PeerConnection createPeerConnection(PeerConnection.Observer observer){
        return peerConnectionFactory.createPeerConnection(iceServer,observer);
    }

    //initilizing ui like surface view renderers
    public void initSurfaceViewRender(SurfaceViewRenderer viewRenderer){
        viewRenderer.setEnableHardwareScaler(true);
        viewRenderer.setMirror(true);
        viewRenderer.init(eglBaseContext,null);
    }

    public void initLocalSurfaceView(SurfaceViewRenderer view){
        initSurfaceViewRender(view);
        startLocalVideoStreaming(view);
    }

    private void startLocalVideoStreaming(SurfaceViewRenderer view) {
        SurfaceTextureHelper helper= SurfaceTextureHelper.create(
                Thread.currentThread().getName(), eglBaseContext
        );

        videoCapturer = getVideoCapturer();
        videoCapturer.initialize(helper,context,localVideoSource.getCapturerObserver());
        videoCapturer.startCapture(480,360,15);
        localVideoTrack = peerConnectionFactory.createVideoTrack(
                localTrackId+"_video",localVideoSource
        );
        localVideoTrack.addSink(view);

        localAudioTrack = peerConnectionFactory.createAudioTrack(localTrackId+"_audio",localAudioSource);
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId);
        localStream.addTrack(localVideoTrack);
        localStream.addTrack(localAudioTrack);
        peerConnection.addStream(localStream);
    }

    private CameraVideoCapturer getVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);

        String[] deviceNames = enumerator.getDeviceNames();

        for (String device: deviceNames){
            if (enumerator.isFrontFacing(device)){
                return enumerator.createCapturer(device,null);
            }
        }
        throw new IllegalStateException("front facing camera not found");
    }

    public void initRemoteSurfaceView(SurfaceViewRenderer view){
        initSurfaceViewRender(view);
    }

    //negotiation section like call and answer
    public void call(String targetUid){
        try{
            peerConnection.createOffer(new MySdpObserver(){
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    peerConnection.setLocalDescription(new MySdpObserver(){
                        @Override
                        public void onSetSuccess() {
                            super.onSetSuccess();
                            //its time to transfer this sdp to other peer
//                            if (listener!=null){

                                CallModel callModel = new CallModel(targetUid, otherName, myId,
                                        username, sessionDescription.description, DataModelType.Offer, true);
//                                listener.onTransferDataToOtherPeer();
                                refCall.child(targetUid).child(myId).setValue(gson.toJson(callModel));
                                
//                                System.out.println("what is c " + sessionDescription.description);
//                            }
                        }
                    },sessionDescription);
                }
            },mediaConstraints);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void answer(String targetUid){
        try{
            peerConnection.createAnswer(new MySdpObserver(){
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    peerConnection.setLocalDescription(new MySdpObserver(){
                        @Override
                        public void onSetSuccess() {
                            super.onSetSuccess();
                            CallModel callModel = new CallModel(targetUid, otherUid, myId, username,
                                    sessionDescription.description, DataModelType.Answer, false);
//                                listener.onTransferDataToOtherPeer();
                            refCall.child(targetUid).child(myId).setValue(gson.toJson(callModel));

                        }
                    },sessionDescription);
                }
            },mediaConstraints);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onRemoteSessionReceived(SessionDescription sessionDescription){
        peerConnection.setRemoteDescription(new MySdpObserver(),sessionDescription);
    }

    public void addIceCandidate(IceCandidate iceCandidate){
        peerConnection.addIceCandidate(iceCandidate);
    }

    public void sendIceCandidate(IceCandidate iceCandidate, String targetUid){
        addIceCandidate(iceCandidate);
        CallModel callModel = new CallModel(targetUid, otherName, myId, username,  gson.toJson(iceCandidate), DataModelType.IceCandidate, false);
        refCall.child(targetUid).child(myId).setValue(gson.toJson(callModel));
    }

    public void switchCamera() {
        videoCapturer.switchCamera(null);
    }

    public void toggleVideo(Boolean shouldBeMuted){
        localVideoTrack.setEnabled(shouldBeMuted);
    }

    public void toggleAudio(Boolean shouldBeMuted){
        localAudioTrack.setEnabled(shouldBeMuted);
    }

    public void closeConnection(){
        try{
            localVideoTrack.dispose();
            videoCapturer.stopCapture();
            videoCapturer.dispose();
            peerConnection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public interface Listener {
        void onTransferDataToOtherPeer(CallModel model);
    }
}

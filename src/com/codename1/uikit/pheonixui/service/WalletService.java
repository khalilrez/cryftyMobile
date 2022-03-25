package com.codename1.uikit.pheonixui.service;

import com.codename1.io.*;
import com.codename1.ui.events.ActionListener;
import com.codename1.uikit.pheonixui.model.Wallet;
import com.codename1.uikit.pheonixui.utils.Statics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WalletService {
    public ConnectionRequest request;
    public boolean resultOK;
    private ArrayList<Wallet> wallets = new ArrayList<>();
    private static WalletService instance;

    private WalletService() {
        request = new ConnectionRequest();
    }

    public static WalletService getInstance() {
     if (instance == null)
         instance = new WalletService();
     return instance;
    }

    public boolean addWallet(Wallet wallet){
        String url = Statics.BASE_URL+"/add";
        request = new ConnectionRequest();
        request.setUrl(url);
        request.setPost(false);
        request.addArgument("label",wallet.getWalletLabel());
        request.addArgument("client",wallet.getClient());
        System.out.println("error 0 here");
        System.out.println("error 0 here");
        System.out.println(request.getRequestBody());

        request.addResponseListener(new ActionListener<NetworkEvent>() {
            @Override
            public void actionPerformed(NetworkEvent evt) {
                resultOK = request.getResponseCode() == 200;
                System.out.println("error 1 here");
                request.removeResponseListener(this);
            }
        });
        NetworkManager.getInstance().addToQueueAndWait(request);
        System.out.println("error 2 here");
        return resultOK;
    }

    public ArrayList<Wallet> getWallets(){
        int id = 1;
        request = new ConnectionRequest();
        String url = Statics.BASE_URL+"/all/"+id;
        request.setUrl(url);
        request.setPost(false);
        request.addResponseListener((evt -> {
            try {
                wallets = parseWallet(new String(request.getResponseData()),id);
            } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }));
        NetworkManager.getInstance().addToQueueAndWait(request);
        return wallets;
    }

    private ArrayList<Wallet> parseWallet(String jsonText, int id) throws IOException, NoSuchFieldException, IllegalAccessException {
        wallets = new ArrayList<>();
        JSONParser j = new JSONParser();
        Map<String,Object> walletListJson = j.parseJSON(new CharArrayReader(jsonText.toCharArray()));
        List<Map<String,Object>> list = (List<Map<String, Object>>) walletListJson.get("root");
        for (Map<String,Object> obj : list){

            Float walletIdF = Float.parseFloat(obj.get("id").toString());
            String walletId = String.valueOf(walletIdF.intValue());
            String walletLabel = obj.get("walletLabel").toString();
            String walletAddress = obj.get("walletAddress").toString();
            String imgPath = obj.get("walletImageFileName").toString();
            boolean isMain = true;
            boolean isActive = true;
            String coinCode;
            if(obj.get("nodeId") != null) {
                Map<String, Object> node = (Map<String, Object>) obj.get("nodeId");
                 coinCode = node.get("coinCode").toString();

            }
            else {
                coinCode = "Not Available";
            }
            Float balance = Float.parseFloat(obj.get("balance").toString());

            Wallet wallet = new Wallet();
            wallet.setId(walletId);
            wallet.setWalletLabel(walletLabel);
            wallet.setClient(Integer.toString(id));
            wallet.setBalance(balance);
            wallet.setCode(coinCode);
            wallet.setWalletAddress(walletAddress);
            wallet.setImgPath(imgPath);
            wallet.setActive(isActive);
            wallet.setMain(isMain);
            wallets.add(wallet);
        }
        return wallets;
    }

    public boolean deleteWallet(int id){
        String url = Statics.BASE_URL+"/delete/"+id;
        request = new ConnectionRequest();
        request.setUrl(url);
        request.setPost(false);
        request.addResponseListener(new ActionListener<NetworkEvent>() {
            @Override
            public void actionPerformed(NetworkEvent evt) {
                resultOK = request.getResponseCode() == 200;
                request.removeResponseListener(this);
            }
        });
        NetworkManager.getInstance().addToQueueAndWait(request);
        return resultOK;
    }

    public boolean updateWallet(Wallet wallet) {

        String url = Statics.BASE_URL+"/update/"+wallet.getId();
        request = new ConnectionRequest();
        request.setUrl(url);
        request.setPost(true);
        request.addArgument("label",wallet.getWalletLabel());
        request.addResponseListener(new ActionListener<NetworkEvent>() {
            @Override
            public void actionPerformed(NetworkEvent evt) {
                resultOK = request.getResponseCode() == 200;
                request.removeResponseListener(this);
            }
        });
        NetworkManager.getInstance().addToQueueAndWait(request);
        return resultOK;
    }
}

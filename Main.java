package main;

import java.io.*;
import java.util.*;


public class Main {

    public static int[][] gridTopo = new int[956][];// 存放原始拓扑信息
    public static int[][] request = new int[4001][];// 存放所有业务需求信息
    public static int UnLimitedMax=99999;

    public static void readTxt() throws IOException {
        String s;
        int i;
        // 1.read gridtopo
        String filePath="gridtopoAndRequest.txt";
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        s = in.readLine();
        i = 0;
        for (i = 0; i < 956; i++) {
            String[] temp = s.split("\\ ");
            gridTopo[i] = new int[temp.length];
            for (int kk = 0; kk < temp.length; kk++) {
                gridTopo[i][kk] = Integer.parseInt(temp[kk]);
            }
            s = in.readLine();
        }
        // 2.read request
        i = 0;
        for(i = 0; i< 4001;i++) {
            String[] temp = s.split("\\ ");
            request[i] = new int[temp.length];
            for (int kk = 0; kk < temp.length; kk++) {
                request[i][kk] = Integer.parseInt(temp[kk]);
            }
            s = in.readLine();
        }
    }

    public static void buildModel(int[][]modelMatrix,int[][]qualityMatrix,int[]kindNum,int[]kindQuality,int[]kindStart,int[]kindEnd,int bakSize){
        int line=gridTopo[0][1];
        int modelSize=gridTopo[0][0];

        int kindSize=request[0][0];


        for(int i=0;i<modelSize;i++){
            Arrays.fill(modelMatrix[i],UnLimitedMax);
            modelMatrix[i][i]=0;
        }
        for(int i=1;i<=line;i++){
            int fromNode=gridTopo[i][0];
            int toNode=gridTopo[i][1];
            int maxQuality=gridTopo[i][2];
            int unitFee=gridTopo[i][3];
            modelMatrix[fromNode][toNode]=unitFee;
            modelMatrix[toNode][fromNode]=unitFee;
            int maxValue=(int)(maxQuality*0.8);
            qualityMatrix[fromNode][toNode]=maxValue;
            qualityMatrix[toNode][fromNode]=maxValue;
        }

        for(int i=0;i<kindSize;i++){
            Arrays.fill(kindQuality,0);
            Arrays.fill(kindStart,0);
            Arrays.fill(kindEnd,0);
        }
        for(int i=0;i<kindSize;i++){
            int kind=request[(bakSize+1)*i+1][0],quality=request[(bakSize+1)*i+1][1];
            int start=request[(bakSize+1)*i+2][0],end=request[(bakSize+1)*i+2][request[(bakSize+1)*i+2].length-1];
            kindNum[i]=kind;
            kindQuality[kind]=quality;
            kindStart[kind]=start;
            kindEnd[kind]=end;
        }

    }

    static void Dijkstra(int kindNUM,int[]kindQuality,int start,int [][]modelMatrix,int[][]qualityMatrix, int []PATH,int []distance,boolean[]flag){
        int modelSize= modelMatrix.length;
        Arrays.fill(PATH,-1);
        for(int i=0;i<modelSize;i++){
            flag[i]=false;
            distance[i] = modelMatrix[start][i];
            if(distance[i]!=UnLimitedMax){
                PATH[i]=start;
            }else{
                PATH[i]=-1;
            }
        }
        flag[start]=true; distance[start]=0;
        for(int i=0;i<modelSize;i++){
            int t=start,minn=UnLimitedMax;
            for(int j=0;j<modelSize;j++){
                if((!flag[j])&&(distance[j]<minn)){
                    t=j;
                    minn=distance[j];
                }
            }
            if(t==start){
                PATH[start]=-1;
                return;
            }
            flag[t]=true;

            for(int j=0;j<modelSize;j++){
                if((modelMatrix[t][j]<UnLimitedMax)&&(!flag[j])){
                    if(distance[j]>(distance[t]+modelMatrix[t][j])&&(qualityMatrix[t][j]>=0)){

                        distance[j]=distance[t]+modelMatrix[t][j];
                        PATH[j]=t;
                    }
                }
            }
        }
    }

    static ArrayList<Integer> getPATH(int crossIdFrom,int crossIdTo,int[]PATH,int[][]modelMatrix,int[]minSum,int kindNum){
        int modelSize=PATH.length;
        ArrayList<Integer>pathFromTo=new ArrayList<>();
        Stack<Integer>stack=new Stack<>();

        int p=0;
        for(int i=0;i<modelSize;i++) {
            if (crossIdFrom == i)
                continue;
            if (i == crossIdTo) {
                p = PATH[i];
                //     System.out.print(crossIdFrom + "---->" + i + "=====");
                while (p != -1) {
                    stack.push(p);
                    p = PATH[p];
                }
                while (!stack.isEmpty()) {
                    pathFromTo.add(stack.pop());
                    //       System.out.print("-->" + stack.pop());
                }
                pathFromTo.add(i);
//                System.out.println("-->" + i + "  ");
            }
        }
        int sum=0;
        for(int i=0;i<pathFromTo.size()-1;i++){
            sum+=modelMatrix[pathFromTo.get(i)][pathFromTo.get(i+1)];
        }
        minSum[kindNum]=sum;
        return pathFromTo;
    }


    public static void main(String[] args) throws IOException {

        //1.输入
        readTxt();
        int modelSize=gridTopo[0][0];

        int[][]modelMatrix=new int[modelSize][modelSize];
        int[][]qualityMatrix=new int[modelSize][modelSize];
        boolean flag[]=new boolean[modelSize];
        int []distance=new int[modelSize];
        int PATH[]=new int[modelSize];

        int kindSize=request[0][0];
        int bakSize=request[0][1];
        int[]kindNum=new int[kindSize];
        int[]kindQuality=new int[kindSize];
        int[]kindStart=new int[kindSize];
        int[]kindEnd=new int[kindSize];
        int minSum[]=new int[kindSize];
     //   buildModel(modelMatrix,qualityMatrix,kindNum,kindQuality,kindStart,kindEnd,bakSize);
        //2.write you code

        int resultKindId[]=new int[kindSize];
        int resultKindQuality[]=new int[kindSize];
        HashMap<Integer,ArrayList<Integer>>resultPath=new HashMap<>();

        int sum=0;
        for(int i=0;i<kindSize;i++){
            ArrayList<Integer>res=new ArrayList<>();
            buildModel(modelMatrix,qualityMatrix,kindNum,kindQuality,kindStart,kindEnd,bakSize);            int kindNUM=kindNum[i];
            int start=kindStart[i],end=kindEnd[i];
            Dijkstra(kindNUM,kindQuality,start,modelMatrix,qualityMatrix,PATH,distance,flag);
            res=getPATH(start,end,PATH,modelMatrix,minSum,kindNUM);
            sum+=kindQuality[i]*minSum[kindNUM];
            resultKindId[i]=kindNUM; resultKindQuality[i]=kindQuality[i];
            resultPath.put(i,res);
        }

        /**print the result**/

        BufferedWriter bf=new BufferedWriter(new FileWriter("result.txt"));
        System.out.println(sum);
        bf.write(sum+""); bf.newLine();
        for(int i=0;i<kindSize;i++){
            System.out.println(resultKindId[i]+" "+resultKindQuality[i]);
            bf.write(resultKindId[i]+" "+resultKindQuality[i]); bf.newLine();
            ArrayList<Integer>res=resultPath.get(i);
            int j=0;
            for(j=0;j<res.size()-1;j++){
                System.out.print(res.get(j)+" ");
                bf.write(res.get(j)+" ");
            }
            System.out.println(res.get(j));
            bf.write(res.get(j)+""); bf.newLine();
        }
        bf.flush();
        bf.close();
    }
}
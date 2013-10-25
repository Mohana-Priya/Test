import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileSorter {
	public static void main(String arg[]){					
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter the fully qualified file name");
			String filePath = br.readLine();
			System.out.println("Enter the RAM size in bytes");
			int memorySize = Integer.parseInt(br.readLine());			
			
			InputStream fileInputStream = new FileInputStream(filePath);
			
			if((long)memorySize*memorySize/fileInputStream.available()==0) {
				System.out.println("In sufficient RAM to sort the input file");
				System.exit(1);
			}
			
			System.out.println("Please wait when sorting the input file");
			int numberOfChunks = 1;
			byte[] b = new byte[memorySize];
			
			/*let 
			 * file size=1024MB
			 * RAM = 500MB
			 * read 500MB of data from i/p file -->sort--> save that sorted bytes to intermediate file
			 * repeat until all the bytes of input file are read*/
			while(fileInputStream.available() >= memorySize) {				
				fileInputStream.read(b, 0,memorySize);
				java.util.Arrays.sort(b);		
				OutputStream fileOutputStream = new FileOutputStream("D:/intermediateFile_" + numberOfChunks++ +".txt");
				fileOutputStream.write(b);
				fileOutputStream.close();
			}
			if(fileInputStream.available() > 0) {
				b = new byte[fileInputStream.available()];
				fileInputStream.read(b, 0, fileInputStream.available());
				java.util.Arrays.sort(b);		
				OutputStream fileOutputStream = new FileOutputStream("D:/intermediateFile_" + numberOfChunks++ +".txt");
				fileOutputStream.write(b);
				fileOutputStream.close();
			}
			fileInputStream.close();
			
			/*
			 * so total number of intermediate files= 3(500MB+500MB+24MB)
			 * now create 3 input buffers and one output buffer of size 125MB(500MB/4)
			*/
			
			int bufSize = memorySize/numberOfChunks;
			int availablemem = memorySize;
			InputStream[] fileInputStreamArray = new FileInputStream[numberOfChunks-1];			
			OutputStream finalFileOS = new FileOutputStream("D:/outputSortedFile.txt");
			byte[][] inputBuffers = new byte[numberOfChunks-1][];
			for(int i=0;i<numberOfChunks-1;i++){
				fileInputStreamArray[i] = new FileInputStream("D:/intermediateFile_" + (i+1) +".txt");
				if(fileInputStreamArray[i].available()>=bufSize){
					inputBuffers[i] = new byte[bufSize];
					fileInputStreamArray[i].read(inputBuffers[i],0,bufSize);
					availablemem -= bufSize;
				}else if(fileInputStreamArray[i].available()>0){
					inputBuffers[i] = new byte[fileInputStreamArray[i].available()];
					fileInputStreamArray[i].read(inputBuffers[i],0,fileInputStreamArray[i].available());
					availablemem -= fileInputStreamArray[i].available();
				}
			}
			byte[] outputBuffer = new byte[availablemem];
			
			/*
			 * Merge sort all the input buffers into output buffer
			 * refill input buffers if they are emptied with their respective files
			 * flush output buffer to the output file once it is full*/			
			int[] inputBufferIndex = new int[numberOfChunks-1];
			for(int i=0;i<inputBufferIndex.length;i++) {
				if(inputBuffers[i]!=null && inputBuffers[i].length>0){
					inputBufferIndex[i]=0;
				} else {
					inputBufferIndex[i]=-1;
				}
				
			}
			int outpuBufferIndex = 0;
			while(outpuBufferIndex >= 0){
				
				int i;
				for(i=0;i<inputBufferIndex.length;i++){				
					if(inputBufferIndex[i] > -1){
						outputBuffer[outpuBufferIndex] = inputBuffers[i][inputBufferIndex[i]];						
						break;
					}
				}
				
				if(i == inputBufferIndex.length){
					if(outpuBufferIndex>0){
						finalFileOS.write(outputBuffer, 0, outpuBufferIndex);
					}
					System.out.println("File sorted successfully and saved in d:/outputSortedFile.txt file");
					finalFileOS.close();
					System.exit(0);
				}
				
				//Find minimum of all current input buffer bytes and assign it to output buffer 
				for(int k=i+1;k<inputBufferIndex.length;k++){
					if(inputBufferIndex[k]!=-1 && outputBuffer[outpuBufferIndex] > inputBuffers[k][inputBufferIndex[k]]) {
						outputBuffer[outpuBufferIndex] = inputBuffers[k][inputBufferIndex[k]];						
						i=k;				
					}
				}
				inputBufferIndex[i] = inputBufferIndex[i]+1;
				
				//If output buffer is full => flush to output file else increment output buffer index
				if(outpuBufferIndex+1 == availablemem){
					finalFileOS.write(outputBuffer);
					outpuBufferIndex = 0;					
				}else if(outpuBufferIndex+1 < availablemem){
					outpuBufferIndex++;
				}
				
				//If input buffer is full =>read next set of available bytes from its corresponding file
				if(inputBufferIndex[i] == inputBuffers[i].length){
					if(fileInputStreamArray[i].available() >= bufSize){
						fileInputStreamArray[i].read(inputBuffers[i], 0, bufSize);
						inputBufferIndex[i] = 0;
					} else if(fileInputStreamArray[i].available() >0) {
						inputBuffers[i] = new byte[fileInputStreamArray[i].available()];
						fileInputStreamArray[i].read(inputBuffers[i], 0, fileInputStreamArray[i].available());
						inputBufferIndex[i] = 0;
					} else {
						inputBufferIndex[i] = -1;
					}
				}
			}			
		} catch (Exception e) {		
			e.printStackTrace();
		}
	}	
}
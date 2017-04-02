import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class InstructionSet extends ArrayList<Instruction>{

	private String filename;

	public InstructionSet(String filename){

		this.filename = filename;

		// parse file
		this.parseFile();
	}

	private void parseFile(){

		try{

			FileReader fr = new FileReader(this.filename);
			BufferedReader br = new BufferedReader(fr);
			String line;

			// read through file
			while((line = br.readLine()) != null){

				String[] splitLine = line.split(" ");
				Instruction node = new Instruction(Long.parseUnsignedLong(splitLine[0], 16), splitLine[1].charAt(0));
				//node.printNodeInfo();
				this.add(node);
			}

			// get the number of lines
			//System.out.println(this.size());

		}catch(Exception e){ System.out.println(e.getMessage()); }

	}

	// MARK: Getters

	public String getFilename(){

		return this.filename;
	}

	public int getNumLines(){

		return this.size();
	}

	public static void main(String[] args){

		InstructionSet bzip = new InstructionSet("bzip.trace");
		//InstructionSet gcc = new TrInstructionSetace("gcc.trace");
	}

}

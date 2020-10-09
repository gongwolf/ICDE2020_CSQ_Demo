package cs.nmsu.edu.demo.RstarTree;


/**
 * Blockfile
 *
 * the block of the RTDirNode is organised as follows:
 * +-------------+------------------+
 * | first block | Rtree blocks.... |
 * +-------------+------------------+
 *
 * the first block of the Blockfile is organised as follows:
 * +----------------+-------------+---------------+
 * | blocklength(4) | numblocks(4)| Rtree header  |
 * +----------------+-------------+---------------+
 */

import java.io.*;
import java.lang.*;

public class BlockFile {

    private final int BFHEAD_LENGTH = 8;
    RandomAccessFile fp;
    private String filename;
    private int blocklength;
    private int act_block;
    private int offset = 0;

    private int number;  //number of blocks in file
    private boolean new_flag;

    //If tree is existed, read blocklenght and number of block
    //Else create a random access file, wirte one block which stored blocklength and number of block is 0
    public BlockFile(String name, int b_length) throws IOException {
        byte[] buffer;
        int l;

        filename = new String(name);
        blocklength = b_length;
        number = 0;

        try {
            fp = new RandomAccessFile(name, "rw");
        } catch (Exception e) {
            System.out.println(e);
        }

//        System.out.println("fp is null ? "+fp!=null);
//        System.out.println("fp length is "+ fp.length());
//        System.out.println("b lenght : "+ b_length);
        if ((fp != null) && (fp.length() != 0) && (b_length == 0)) // file already exists - read info from file
        {
            new_flag = false;
            blocklength = fread_number();
            number = fread_number();
        } else {
            if (blocklength < BFHEAD_LENGTH) {
                System.out.println("BlockFile::BlockFile: Blocks");
            }
            fp = new RandomAccessFile(name, "rw");
            if (!(fp.getFD()).valid()) {
                System.out.println("BlockFile:: new_file: Schreibfehler");
            }
            new_flag = true;
            // write blockfile info
            fwrite_number(blocklength);
            fwrite_number(number);
            // fill the rest of the block with 0s
//            System.out.println("Initial fp Pointer :"+fp.getFilePointer());
//            System.out.println("remaining number of bytes :"+ (blocklength-(int)fp.getFilePointer()));
            buffer = new byte[l = blocklength - (int) fp.getFilePointer()];
            for (int i = 0; i < l; i++) {
                buffer[i] = 0;
            }
            put_bytes(buffer, l);
        }
        fp.seek(0);
        act_block = 0;
    }

    //put size of data to file by position fp_pointer
    private void put_bytes(byte[] data, int size) throws IOException {
        fp.write(data, 0, size);
    }

    private void get_bytes(byte[] data, int size) throws IOException {
        fp.read(data, 0, size);
    }

    private void fwrite_number(int value) throws IOException {
        fp.writeInt(value);
    }

    private int fread_number() throws IOException {
        return fp.readInt();
    }

    private void seek_block(int bnum) throws IOException {
        fp.seek((bnum - act_block) * blocklength + (int) fp.getFilePointer());
//        System.out.println("seek_block:" + ((bnum - act_block) * blocklength + (int) fp.getFilePointer()));
    }

    public void read_header(byte[] buffer) throws IOException {
        // read Rtree header
        fp.seek(BFHEAD_LENGTH);
//        System.out.println("BFHEAD_LENGTH:"+BFHEAD_LENGTH);
        get_bytes(buffer, blocklength - BFHEAD_LENGTH);
//        System.out.println("blocklength-BFHEAD_LENGTH:"+ (blocklength-BFHEAD_LENGTH));

        if (number < 1) {
            fp.seek(0);
            act_block = 0;
        } else {
            act_block = 1;
        }
    }

    public void set_header(byte[] header) throws IOException {
        // write Rtree header to header
        fp.seek(BFHEAD_LENGTH);
        put_bytes(header, blocklength - BFHEAD_LENGTH);

        if (number < 1) {
            fp.seek(0);
            act_block = 0;
        } else {
            act_block = 1;
        }
    }

    public boolean read_block(byte[] b, int pos) throws IOException {
        pos++;
        if (pos <= number && pos > 0) {
            seek_block(pos);
        } else {
            return false;
        }

        get_bytes(b, blocklength);

        if (pos + 1 > number) {
            fp.seek(0);
            act_block = 0;
        } else {
            act_block = pos + 1;
        }

        return true;
    }

    public boolean write_block(byte[] block, int pos) throws IOException {
        pos++;

        if (pos <= number && pos > 0) {
            seek_block(pos);
        } else {
            return false;
        }

        put_bytes(block, blocklength);
        if (pos + 1 > number) {
            fp.seek(0);
            act_block = 0;
        } else {
            act_block = pos + 1;
        }
        return true;
    }

    //append the block to the end of the file 
    public int append_block(byte[] block) throws IOException {
        // go to the end of file
        fp.seek(fp.length());
        // write new block     
        put_bytes(block, blocklength);
        // update number information (also write it to file)
        number++;
        fp.seek(4);
        fwrite_number(number);
        // position at the new inserted block
        fp.seek((int) fp.length() - blocklength);

        // return the active block index
        // because the first block just the information of this tree
        // So the actual number is number-1;
        return (act_block = number) - 1;
    }

    public boolean delete_last_blocks(int num) throws IOException {
        RandomAccessFile tmp_fp;
        byte[] buffer;
        int blocks_read = 0;

        if (num > number) {
            return false;
        }

        fp.seek(0);

        tmp_fp = new RandomAccessFile(filename + ".tmp", "rw");
        buffer = new byte[blocklength];
        int c = 0;
        while ((blocks_read < number - num + 1) && (c != -1)) {
            if ((c = fp.read(buffer)) != -1) {
                tmp_fp.write(buffer);
                blocks_read++;
            }
        }
        fp.close();

        fp = new RandomAccessFile(filename, "rw");
        c = 0;
        blocks_read = 0;
        tmp_fp.seek(0);
        while ((blocks_read < number - num + 1) && (c != -1)) {
            if ((c = tmp_fp.read(buffer)) != -1) {
                fp.write(buffer);
                blocks_read++;
            }
        }
        tmp_fp.close();

        number -= num;
        fp.seek(BFHEAD_LENGTH);
        fwrite_number(number);
        fp.seek(0);
        act_block = 0;
        return true;
    }

    public boolean file_new() {
        return new_flag;
    }

    public int get_blocklength() {
        return blocklength;
    }

    public int get_num_of_blocks() {
        return number;
    }
}

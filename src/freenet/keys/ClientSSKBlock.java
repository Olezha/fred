package freenet.keys;

import java.io.IOException;

import freenet.crypt.PCFBMode;
import freenet.crypt.UnsupportedCipherException;
import freenet.crypt.ciphers.Rijndael;
import freenet.support.Bucket;
import freenet.support.BucketFactory;

public class ClientSSKBlock extends SSKBlock implements ClientKeyBlock {
	
	static final int DATA_DECRYPT_KEY_LENGTH = 32;
	
	static public final int MAX_DECOMPRESSED_DATA_LENGTH = 32768;
	
	/** Is metadata. Set on decode. */
	private boolean isMetadata;
	/** Has decoded? */
	private boolean decoded;
	/** Client-key. This contains the decryption key etc. */
	private ClientSSK key;
	
	public ClientSSKBlock(byte[] data, byte[] headers, ClientSSK key, boolean dontVerify) throws SSKVerifyException {
		super(data, headers, (NodeSSK) key.getNodeKey(), dontVerify);
		this.key = key;
	}
	
	public ClientSSKBlock(SSKBlock block, ClientSSK key) throws SSKVerifyException {
		this(block.data, block.headers, key, false);
	}
	
	/**
	 * Decode the data.
	 */
	public Bucket decode(BucketFactory factory, int maxLength) throws KeyDecodeException, IOException {
		/* We know the signature is valid because it is checked in the constructor. */
		/* We also know e(h(docname)) is valid */
		byte[] decryptedHeaders = new byte[ENCRYPTED_HEADERS_LENGTH];
		System.arraycopy(headers, headersOffset, decryptedHeaders, 0, ENCRYPTED_HEADERS_LENGTH);
		Rijndael aes;
		try {
			aes = new Rijndael(256,256);
		} catch (UnsupportedCipherException e) {
			throw new Error(e);
		}
		aes.initialize(key.cryptoKey);
		PCFBMode pcfb = new PCFBMode(aes);
		// ECB-encrypted E(H(docname)) serves as IV.
		pcfb.reset(key.ehDocname);
		pcfb.blockDecipher(decryptedHeaders, 0, decryptedHeaders.length);
		// First 32 bytes are the key
		byte[] dataDecryptKey = new byte[DATA_DECRYPT_KEY_LENGTH];
		System.arraycopy(decryptedHeaders, 0, dataDecryptKey, 0, DATA_DECRYPT_KEY_LENGTH);
		aes.initialize(dataDecryptKey);
		byte[] dataOutput = new byte[data.length];
		System.arraycopy(data, 0, dataOutput, 0, data.length);
		// Data decrypt key should be unique, so use it as IV
		pcfb.reset(dataDecryptKey);
		pcfb.blockDecipher(dataOutput, 0, dataOutput.length);
		// 2 bytes - data length
		int dataLength = ((decryptedHeaders[DATA_DECRYPT_KEY_LENGTH] & 0xff) << 8) +
			(decryptedHeaders[DATA_DECRYPT_KEY_LENGTH+1] & 0xff);
		// Metadata flag is top bit
		if((dataLength & 32768) != 0) {
			dataLength = dataLength & ~32768;
			isMetadata = true;
		}
		if(dataLength > data.length) {
			throw new SSKDecodeException("Data length: "+dataLength+" but data.length="+data.length);
		}
		
		if(dataLength != data.length) {
			byte[] realDataOutput = new byte[dataLength];
			System.arraycopy(dataOutput, 0, realDataOutput, 0, dataLength);
			dataOutput = realDataOutput;
		}
        short compressionAlgorithm = (short)(((decryptedHeaders[DATA_DECRYPT_KEY_LENGTH+2] & 0xff) << 8) + (decryptedHeaders[DATA_DECRYPT_KEY_LENGTH+3] & 0xff));

        Bucket b = Key.decompress(compressionAlgorithm >= 0, dataOutput, factory, Math.min(MAX_DECOMPRESSED_DATA_LENGTH, maxLength), compressionAlgorithm, true);
        decoded = true;
        return b;
	}

	public boolean isMetadata() {
		if(!decoded)
			throw new IllegalStateException("Cannot read isMetadata before decoded");
		return isMetadata;
	}

	public ClientKey getClientKey() {
		return key;
	}

}

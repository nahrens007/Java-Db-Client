package com.nathanahrens.pwsafe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.pwsafe.lib.exception.EndOfFileException;
import org.pwsafe.lib.exception.InvalidPassphraseException;
import org.pwsafe.lib.exception.UnsupportedFileVersionException;
import org.pwsafe.lib.file.PwsField;
import org.pwsafe.lib.file.PwsFieldTypeV3;
import org.pwsafe.lib.file.PwsFile;
import org.pwsafe.lib.file.PwsFileFactory;
import org.pwsafe.lib.file.PwsRecord;

public class SafeWrapper {
	PwsFile safe;

	public SafeWrapper(String file, StringBuilder passphrase) {
		if (file == null || passphrase == null) {
			System.out.println("ERROR: Must provide a non-null file path and passphrase.");
		}
		if (!load(file, passphrase)) {
			System.out.println("ERROR: Unable to load vault...");
		}
	}

	public boolean load(String file, StringBuilder passphrase) {
		try {
			this.safe = PwsFileFactory.loadFile(file, passphrase);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		} catch (EndOfFileException e) {
			e.printStackTrace();
			return false;
		} catch (InvalidPassphraseException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedFileVersionException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Credential getCredential(String group, String title) {
		if (title == null) {
			System.out.println("Must provide a title.");
			return null;
		}
		int recordCount = this.safe.getRecordCount();
		PwsRecord rec;
		for (int i = 0; i < recordCount; i++) {
			rec = this.safe.getRecord(i);
			PwsField recGroup = rec.getField(PwsFieldTypeV3.GROUP);
			PwsField recTitle = rec.getField(PwsFieldTypeV3.TITLE);
			if (group == null) {
				// We want records without GROUP type fields
				if (recGroup == null && recTitle != null && recTitle.toString().equals(title)) {
					// Fields where recTitle = title.
					return new Credential(rec.getField(PwsFieldTypeV3.USERNAME).toString(),
							rec.getField(PwsFieldTypeV3.PASSWORD).toString());
				}
			} else {
				// Records in a GROUP
				if (recGroup != null && recGroup.toString().equals(group) && recTitle != null
						&& recTitle.toString().equals(title)) {
					// Title and group match
					return new Credential(rec.getField(PwsFieldTypeV3.USERNAME).toString(),
							rec.getField(PwsFieldTypeV3.PASSWORD).toString());
				}
			}

		}
		// If this point is reached, group or title does not exist.
		return null;
	}
}

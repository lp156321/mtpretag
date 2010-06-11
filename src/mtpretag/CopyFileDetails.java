//
//    Copyright 2010 Paul White
//
//    This file is part of MTPRetag.
//
//    MTPRetag is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.

//    MTPRetag is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with MTPRetag.  If not, see <http://www.gnu.org/licenses/>.
//

package mtpretag;

import jpmp.device.FileMetadata;

/**
 * This class stores details about a file which is ready
 * for copying.
 */
public class CopyFileDetails {

    public String getDestination() {
        return destination;
    }

    public FileMetadata getFm() {
        return fm;
    }

    public String getSource() {
        return source;
    }

    public TransferNotifier getTn() {
        return tn;
    }

    public CopyFileDetails(String source, String destination, FileMetadata fm, TransferNotifier tn) {
        this.source = source;
        this.destination = destination;
        this.fm = fm;
        this.tn = tn;
    }
    protected String source;       // source filename
    protected String destination;  // target path
    protected FileMetadata fm;     // tag info for the destination
    protected TransferNotifier tn; // where to notify transfer progress
}

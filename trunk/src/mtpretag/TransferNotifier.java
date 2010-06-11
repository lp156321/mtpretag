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

/**
 * This class will be notified of transfer progress for a particular file.
 * This is currently not in use.
 */
public class TransferNotifier implements jpmp.notifier.IDeviceTransferNotifier  {

    public boolean getAbort() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return false;
    }

    public void notifyBegin(long l) {
        //System.out.println("java: notify begin " + l);
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void notifyCurrent(long l) {
        //System.out.println("java: notify current " + l);
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void notifyEnd() {
        //System.out.println("java: notify end");
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}


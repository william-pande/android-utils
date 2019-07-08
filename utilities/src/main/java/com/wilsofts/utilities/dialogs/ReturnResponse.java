package com.wilsofts.utilities.dialogs;

import java.io.Serializable;

public interface ReturnResponse extends Serializable {
    void response(boolean proceed);
}

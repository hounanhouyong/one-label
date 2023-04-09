package com.hn.onelabel.server.service;

import com.hn.onelabel.api.model.request.SaveUserSegmentInfoRequest;

import java.util.List;

public interface UserSegmentCommandService {

    void saveUserSegmentInfo(SaveUserSegmentInfoRequest request);

    void disableUserSegment(List<Long> segmentIds);
}

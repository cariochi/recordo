package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;

import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;

public interface GitHub {

    List<GistResponse> getGists();

    Gist getGist(String id, String rand);

    GistResponse createGist(Gist gist);

    GistResponse updateGist(String id, Gist gist);

    void deleteGist(String id);

}

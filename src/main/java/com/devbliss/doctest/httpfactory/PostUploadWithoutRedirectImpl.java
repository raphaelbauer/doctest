/*
 * Copyright 2013, devbliss GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.devbliss.doctest.httpfactory;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import de.devbliss.apitester.factory.PostFactory;

/**
 * Implements a POST HTTP request which does not handle any redirect.
 * 
 * @author bmary
 * 
 */
public class PostUploadWithoutRedirectImpl implements PostFactory {

    private final String paramName;
    private final FileBody fileBodyToUpload;

    public PostUploadWithoutRedirectImpl(String paramName, FileBody fileToUpload) {
        this.paramName = paramName;
        this.fileBodyToUpload = fileToUpload;
    }

    public HttpPost createPostRequest(URI uri, Object payload) throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        HttpParams params = new BasicHttpParams();
        params.setParameter(HttpConstants.HANDLE_REDIRECTS, false);
        httpPost.setParams(params);

        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        entity.addPart(paramName, fileBodyToUpload);
        httpPost.setEntity(entity);

        return httpPost;
    }

}

/********************************************************************************
 * Copyright (c) 2011, Scott Ferguson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the software nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.ferg.awful;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ferg.awful.constants.Constants;
import com.ferg.awful.reply.Reply;
import com.ferg.awful.thread.AwfulThread;

public class PostReplyActivity extends Activity {
    private static final String TAG = "PostReplyActivity";

    private Button mSubmit;
    private EditText mMessage;
	private TextView mTitle;

	private AwfulThread mThread;
	private String mFormKey;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_reply);

        mSubmit  = (Button) findViewById(R.id.submit_button);
        mMessage = (EditText) findViewById(R.id.post_message);
		mTitle   = (TextView) findViewById(R.id.title);

        Intent caller = getIntent();

		mThread = (AwfulThread) caller.getParcelableExtra(Constants.THREAD);
		
		mTitle.setText(getString(R.string.post_reply));

        // If we're quoting a post, add it to the message box
        if (caller.hasExtra(Constants.QUOTE)) {
            String quoteText = caller.getStringExtra(Constants.QUOTE);
            mMessage.setText(quoteText.replaceAll("&quot;", "\""));
        }

		// We'll enable it once we have a formkey
		mSubmit.setEnabled(false);

        mSubmit.setOnClickListener(onSubmitClick);

		new FetchFormKeyTask().execute(mThread.getThreadId());
    }

    private View.OnClickListener onSubmitClick = new View.OnClickListener() {
        public void onClick(View aView) {
			new SubmitReplyTask().execute(mMessage.getText().toString(), 
					mFormKey, mThread.getThreadId());
        }
    };

	private class SubmitReplyTask extends AsyncTask<String, Void, Void> {
		public Void doInBackground(String... aParams) {
			try {
				Reply.postReply(aParams[0], aParams[1], aParams[2]);
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, e.toString());
			}

			return null;
		}

		public void onPostExecute(Void aResult) {
			Log.i(TAG, "Done!");

            PostReplyActivity.this.finish();
		}
	}

	private class FetchFormKeyTask extends AsyncTask<String, Void, String> {
		public String doInBackground(String... aParams) {
			String result = null;

			try {
				result = Reply.getFormKey(aParams[0]);
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, e.toString());
			}

			return result;
		}

		public void onPostExecute(String aResult) {
			if (aResult.length() > 0) {
				Log.i(TAG, aResult);

				mFormKey = aResult;
				mSubmit.setEnabled(true);
			}
		}
	}
}

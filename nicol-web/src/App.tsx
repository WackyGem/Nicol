import React, { useState, useRef, useEffect } from 'react';
import './App.css';
import { SSE } from 'sse.js';

function App() {
  const [prompt, setPrompt] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState('');
  const resultRef = useRef<string>(result);
  useEffect(() => {
    resultRef.current = result;
  }, [result]);
  const handlePromptChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const inputValue = e.target.value;
    setPrompt(inputValue);
  };

  const handleClearBtnClicked = () => {
    setPrompt('');
    setResult('');
  };

  const handleSubmitPromptBtnClicked = async () => {
    if (prompt !== '') {
      setIsLoading(true);
      setResult(prompt);
      const url = `${import.meta.env.VITE_BACKEND_API}/api/v1/chat/completions`;
      const data = {
        model: 'llama2',
        messages: [
          {
            role: 'user',
            content: prompt,
          },
        ],
        temperature: 0.75,
        top_p: 0.95,
        max_tokens: 256,
        stream: true,
        n: 1,
      };

      const source = new SSE(url, {
        headers: {
          'Content-Type': 'application/json',
        },
        method: 'POST',
        payload: JSON.stringify(data),
      });
      resultRef.current = prompt;
      source.addEventListener('message', (e: MessageEvent) => {
        if (e.data !== '[DONE]') {
          const payload = JSON.parse(e.data);
          const text = payload.choices[0].delta.content;
          if (
            text !== '/n' &&
            payload.choices[0].finishReason === null &&
            resultRef.current !== undefined
          ) {
            resultRef.current += text;
            setResult(resultRef.current);
          }
        } else {
          source.close();
        }
      });

      source.addEventListener('readystatechange', (e: EventSource) => {
        if (e.readyState >= 2) {
          setIsLoading(false);
        }
      });
      source.stream();
    }
  };

  return (
    <div className="app-container">
      <div className="app-card">
        <h1 className="app-heading">Nicol</h1>
        <p className="app-description">
          A demo developed in Kotlin for showcasing
        </p>
        <p>
          the Llama2 streaming inference service
        </p>
        <textarea
          value={prompt}
          onChange={handlePromptChange}
          placeholder="Insert your prompt here ..."
          className="app-textarea"
        />
        <div className="button-container">
          <button
            type="button"
            className="app-button"
            onClick={handleSubmitPromptBtnClicked}
            disabled={isLoading}
          >
            {isLoading ? 'Loading...' : 'Submit Prompt'}
          </button>
          <button
            type="button"
            className="app-button"
            onClick={handleClearBtnClicked}
          >
            Clear
          </button>
        </div>
        {result !== '' && (
          <div className="app-result">
            <h5 className="result-heading">Result:</h5>
            <p className="result-text">{result}</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;

import { useState } from "react";

function App() {
  const [task, setTask] = useState("");
  const [tasks, setTasks] = useState([]);

  const addTask = () => {
    if (task.trim() === "") return;

    setTasks([...tasks, task]);
    setTask("");
  };

  const startVoice = () => {
    const SpeechRecognition =
      window.SpeechRecognition || window.webkitSpeechRecognition;

    const recognition = new SpeechRecognition();

    recognition.lang = "en-US";

    recognition.onresult = (event) => {
      const text = event.results[0][0].transcript;
      setTask(text);
    };

    recognition.start();
  };

  return (
    <div
      style={{
        maxWidth: "700px",
        margin: "50px auto",
        fontFamily: "Arial",
      }}
    >
      <h1>Voice First Task App</h1>

      <div style={{ display: "flex", gap: "10px" }}>
        <input
          type="text"
          placeholder="Enter task"
          value={task}
          onChange={(e) => setTask(e.target.value)}
          style={{
            flex: 1,
            padding: "10px",
            fontSize: "16px",
          }}
        />

        <button onClick={startVoice}>🎤</button>

        <button onClick={addTask}>Add</button>
      </div>

      <ul style={{ marginTop: "30px" }}>
        {tasks.map((t, index) => (
          <li
            key={index}
            style={{
              marginBottom: "10px",
              padding: "10px",
              background: "#f1f1f1",
              borderRadius: "5px",
            }}
          >
            {t}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
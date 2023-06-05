"use client";

import { useEffect, useState } from "react";
import { APIs } from "./apis";

export default function Greeting() {
    const [greetingMessage, setGreetingMessage] = useState("");

    useEffect(() => {
        APIs.greetings.getGreeting().then(response => {
        setGreetingMessage(currentMessage => {
            return response.data?.message || ""
          });
        });
    });
    
    return (<h1>{greetingMessage}</h1>);
}
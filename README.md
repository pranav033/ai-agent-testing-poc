# AI Agent Testing POC

A hands-on Quality Engineering POC for testing an AI-style conversational agent.

The application is a deterministic local AI-agent simulator designed to demonstrate how a QA/Quality Engineer can validate conversational behavior, safety, reliability, and context management without depending on an external LLM API.

## What This POC Demonstrates

- Functional task completion
- Multi-turn context retention
- Ambiguous-input handling
- Unknown-data handling / hallucination resistance
- Safety guardrails
- Prompt-injection resistance
- Session/context isolation
- Response consistency
- Automated regression testing with JUnit

## Architecture

```text
Client
  |
  v
POST /api/chat
  |
  v
ChatController
  |
  v
ChatService
  |---- Guardrail checks
  |---- Order/task handling
  |---- Ambiguity handling
  |---- Unknown-data handling
  |
  v
ConversationService
  |
  v
Session Context
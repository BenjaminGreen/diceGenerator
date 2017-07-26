package com.green.benjamin.diceGenerator;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.green.benjamin.exeptions.InvalidDieSidesException;
import com.green.benjamin.exeptions.InvalidRollsException;
import com.green.benjamin.exeptions.OddDieExceptionException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceGeneratorSpeechlet implements Speechlet {

  public static final Pattern NORMAL_DICE_PATTEN = Pattern.compile("([0-9]{1,} d [0-9]{1,})");
  public static final int MAX_SIDES = 1000;
  public static final int MIN_ROLLS_AND_SIDES = 1;
  public static final int MAX_ROLLS = 5;
  public static final String ENUMERATED = "enumerated";
  public static final int MAX_ENUMERATED_SIDES = 12;
  public static final String STANDARD = "standard";
  private static final String SLOT_DICE_ROLL = "Roll";
  private final DiceService diceService;

  public DiceGeneratorSpeechlet() {
    this.diceService = new DiceService();
  }

  @Override
  public void onSessionStarted(final SessionStartedRequest request, final Session session)
      throws SpeechletException {
    // any initialization logic goes here
  }

  @Override
  public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
      throws SpeechletException {
    return getWelcomeResponse();
  }

  @Override
  public SpeechletResponse onIntent(final IntentRequest request, final Session session)
      throws SpeechletException {

    Intent intent = request.getIntent();
    String intentName = intent.getName();

    if ("rollDiceIntent".equals(intentName)) {
      return handleNormalDiceRolls(intent);
    } else if ("rollEnumeratedDieIntent".equals(intentName)) {
      return handleEnumeratedDieRoll(intent);
    } else if ("AMAZON.HelpIntent".equals(intentName)) {
      return handleHelpRequest();
    } else if ("AMAZON.StopIntent".equals(intentName)) {
      PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
      outputSpeech.setText("Goodbye");

      return SpeechletResponse.newTellResponse(outputSpeech);
    } else if ("AMAZON.CancelIntent".equals(intentName)) {
      PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
      outputSpeech.setText("Goodbye");

      return SpeechletResponse.newTellResponse(outputSpeech);
    } else {
      throw new SpeechletException("Invalid Intent");
    }
  }

  @Override
  public void onSessionEnded(final SessionEndedRequest request, final Session session)
      throws SpeechletException {
  }

  private SpeechletResponse getWelcomeResponse() {
    String rollPrompt = "What dice would you like to roll ?";
    String speechOutput = "<speak>"
        + rollPrompt
        + "</speak>";
    String repromptText =
        "I can roll any sided die between two and one thousand,  "
            + "I can also roll an enumerated die if you specify the sides."
            + rollPrompt;

    return newAskResponse(speechOutput, true, repromptText, false);
  }

  private SpeechletResponse handleHelpRequest() {
    String repromptText = "Which city would you like tide information for?";
    String speechOutput =
        "I can lead you through providing a city and "
            + "day of the week to get tide information, "
            + "or you can simply open Tide Pooler and ask a question like, "
            + "get tide information for Seattle on Saturday. "
            + "For a list of supported cities, ask what cities are supported. "
            + "Or you can say exit. " + repromptText;

    return newAskResponse(speechOutput, repromptText);
  }

  private SpeechletResponse handleEnumeratedDieRoll(final Intent intent) {
    final EnumeratedDie enumeratedDie;
    try {
      enumeratedDie = getEnumeratedDie(intent);
    } catch (final OddDieExceptionException
        | InvalidDieSidesException
        | InvalidRollsException cause) {
      final String speechOutput =
          cause.getMessage() + ", would you like to try again?";
      return newAskResponse(speechOutput, speechOutput);
    } catch (final Exception cause) {
      final String speechOutput =
          "Unable to roll dice, would you like to try again?";
      return newAskResponse(speechOutput, speechOutput);
    }

    return toResponse(Lists.newArrayList(diceService.rollDie(enumeratedDie)));
  }

  private SpeechletResponse handleNormalDiceRolls(final Intent intent) {
    final Map<Integer, Integer> diceToRollsMap;
    try {
      diceToRollsMap = getDiceToRollsMap(intent);
    } catch (final OddDieExceptionException
        | InvalidDieSidesException
        | InvalidRollsException cause) {
      final String speechOutput =
          cause.getMessage() + ", would you like to try again?";
      return newAskResponse(speechOutput, speechOutput);
    } catch (final Exception cause) {
      final String speechOutput =
          "Unable to roll dice, would you like to try again?";
      return newAskResponse(speechOutput, speechOutput);
    }

    return toResponse(diceService.rollDice(diceToRollsMap));
  }

  private SpeechletResponse toResponse(final List<DiceResult> results) {
    final String speechOutput = "";
    final JSONArray diceRollsArray = new JSONArray();
    final JSONObject cardOutput = new JSONObject();
    cardOutput.put("results", diceRollsArray);

    results.forEach(result -> {
      diceRollsArray.add(toJsonObject(result));
    });

    SimpleCard card = new SimpleCard();
    card.setTitle("Dice Generator");
    card.setContent(cardOutput.toJSONString());
    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
    outputSpeech.setText(speechOutput);

    return SpeechletResponse.newTellResponse(outputSpeech, card);
  }

  private Object toJsonObject(final DiceResult result) {
    final JSONObject resultJson = new JSONObject();
    final JSONArray rolls = new JSONArray();
    resultJson.put("die", result.getDieName());
    resultJson.put("rolls", rolls);

    result.getRollResults().forEach(roll ->
        rolls.add(roll)
    );

    return resultJson;
  }

  private Map<Integer, Integer> getDiceToRollsMap(final Intent intent) throws Exception {

    final Slot rollSlot = intent.getSlot(SLOT_DICE_ROLL);
    final Map<Integer, Integer> integerToRolls = Maps.newHashMap();

    final Matcher matches = NORMAL_DICE_PATTEN.matcher(rollSlot.getValue().toLowerCase());

    while (matches.find()) {
      final String[] numbers = matches.group().split("d");
      final Integer dieSides = validateDieSides(numbers[0]);
      final Integer rolls = validateRolls(numbers[1]);
      integerToRolls.put(dieSides, rolls);
    }
    return integerToRolls;
  }

  private EnumeratedDie getEnumeratedDie(final Intent intent) throws Exception {
    final Slot rollSlot = intent.getSlot(SLOT_DICE_ROLL);
    final List<String> enumeratedValues = Lists.newArrayList();

    for (final String side : rollSlot.getValue().toLowerCase().split("and")) {
      enumeratedValues.add(side.trim());
    }

    if (enumeratedValues.size() > MAX_ENUMERATED_SIDES | enumeratedValues.size() < MIN_ROLLS_AND_SIDES) {
      throw new InvalidDieSidesException(enumeratedValues.size(),
          ENUMERATED, MIN_ROLLS_AND_SIDES, MAX_ENUMERATED_SIDES);
    }

    return new EnumeratedDie(enumeratedValues);
  }


  private Integer validateDieSides(final String number) throws InvalidDieSidesException {
    final int sides = parseNumber(number);

    if (sides > MAX_SIDES | sides < MIN_ROLLS_AND_SIDES) {
      throw new InvalidDieSidesException(sides, STANDARD, MIN_ROLLS_AND_SIDES, MAX_SIDES);
    }

    return sides;
  }

  private Integer validateRolls(final String number) throws InvalidRollsException {
    final int rolls = parseNumber(number);

    if (rolls > MAX_ROLLS | rolls < MIN_ROLLS_AND_SIDES) {
      throw new InvalidRollsException(rolls, MIN_ROLLS_AND_SIDES, MAX_SIDES);
    }

    return rolls;
  }

  private Integer parseNumber(final String number) {
    return Integer.valueOf(number);
  }

  private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
    return newAskResponse(stringOutput, false, repromptText, false);
  }

  /**
   * Wrapper for creating the Ask response from the input strings.
   *
   * @param stringOutput   the output to be spoken
   * @param isOutputSsml   whether the output text is of type SSML
   * @param repromptText   the reprompt for if the user doesn't reply or is misunderstood.
   * @param isRepromptSsml whether the reprompt text is of type SSML
   * @return SpeechletResponse the speechlet response
   */
  private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
                                           String repromptText, boolean isRepromptSsml) {
    OutputSpeech outputSpeech, repromptOutputSpeech;
    if (isOutputSsml) {
      outputSpeech = new SsmlOutputSpeech();
      ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
    } else {
      outputSpeech = new PlainTextOutputSpeech();
      ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
    }

    if (isRepromptSsml) {
      repromptOutputSpeech = new SsmlOutputSpeech();
      ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
    } else {
      repromptOutputSpeech = new PlainTextOutputSpeech();
      ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
    }

    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(repromptOutputSpeech);
    return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
  }
}
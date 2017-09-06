/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.isstac.cogito.ml;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.sv.isstac.cogito.Conditional;
import edu.cmu.sv.isstac.cogito.Decision;
import edu.cmu.sv.isstac.cogito.Path;

/**
 * @author Kasper Luckow
 */
public class Training {
  private Collection<Path> wcPaths = new ArrayList<>();
  private Map<Conditional, Integer> dec2idx = new HashMap<>();
  private int nextIndex = 0;

  public void addToTraining(Path path) {
    wcPaths.add(path);
  }

  public Map<Conditional, int[][]> toArray() {
    Set<Conditional> conditionals = new HashSet<>();
    for(Path p : wcPaths) {
      conditionals.addAll(p.getUniqueConditionals());
    }

    Map<Conditional, Collection<int[]>> td = new HashMap<>();

//    int[][] trainingData = new int[wcPaths.size()][conditionals.size() * 2];
    int row = 0;
    for(Path p : wcPaths) {
      int[] data = new int[conditionals.size() * 2];
      for(Decision d : p) {
        Collection<int[]> decisionTrainingData = td.get(d.getCond());
        if(decisionTrainingData == null) {
          decisionTrainingData = new ArrayList<>();
          td.put(d.getCond(), decisionTrainingData);
        }
        decisionTrainingData.add(data);

        int[] newData = new int[conditionals.size() * 2];
        java.lang.System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;

        int idx = getIdx(d);
        data[idx] = data[idx] + 1;
      }
//      trainingData[row++] = data;
    }

    Map<Conditional, int[][]> finalArray = new HashMap<>();
    for(Map.Entry<Conditional, Collection<int[]>> ent : td.entrySet()) {
      int[][] data = ent.getValue().toArray(new int[ent.getValue().size()][conditionals.size() *
          2]);
      finalArray.put(ent.getKey(), data);
    }

    return finalArray;
  }

  private int getIdx(Decision decision) {

    int conditionalIdx;
    if(dec2idx.containsKey(decision.getCond())) {
      conditionalIdx = dec2idx.get(decision.getCond());
    } else {
      conditionalIdx = nextIndex;
      dec2idx.put(decision.getCond(), conditionalIdx);
      nextIndex++;
    }

    int decisionIdx = toDecisionIdx(conditionalIdx, decision.getChoice());
    return decisionIdx;
  }

  private int toDecisionIdx(int conditionalIdx, int choice) {
    //Todo: We constrain ourselves to binary decisions now
    Preconditions.checkArgument(choice == 0 || choice == 1);

    return conditionalIdx * 2 + choice;
  }
}

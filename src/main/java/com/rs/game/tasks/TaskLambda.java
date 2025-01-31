// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.tasks;

public class TaskLambda extends Task {

	private final Runnable task;
	private String stack;

	public TaskLambda(Runnable task) {
		this.task = task;
		stack = "";
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for(int i = 1; i < elements.length; ++i) {
			StackTraceElement s = elements[i];
			String var10001 = s.getClassName();
			stack += "\tat " + var10001 + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")";
		}
	}

	@Override
	public void run() {
		task.run();
	}

	@Override
	public String toString() {
		return "TaskLambda\n" + stack;
	}
}

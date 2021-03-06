package anuled.dynamicstore.sparqlopt;

enum ConstraintType {
	// Either left < right or left <= right
	LESS {
		@Override
		public String toString() {
			return "<";
		}
	},
	LESS_EQ {
		@Override
		public String toString() {
			return "<=";
		}
	};
}